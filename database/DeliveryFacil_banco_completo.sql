/* ============================================================
   DeliveryFácil | Banco de Dados Aplicado | SQL Server 2019+
   Script 01 - Criação do banco e das tabelas (modelo físico)
   Ordem de execução: 01 -> 02 -> 03 -> 04
   ============================================================ */
IF DB_ID('DeliveryFacil') IS NULL CREATE DATABASE DeliveryFacil;
GO
USE DeliveryFacil;
GO
DROP TABLE IF EXISTS HISTORICO_PRODUTO, HISTORICO_STATUS, AVALIACAO, PAGAMENTO, ITEM_OPCAO,
                     ITEM_PEDIDO, PEDIDO, OPCAO, GRUPO_OPCAO, PRODUTO, ENDERECO, AREA_ENTREGA,
                     CLIENTE, FUNCIONARIO;
GO

CREATE TABLE FUNCIONARIO (
    id_funcionario INT IDENTITY(1,1) NOT NULL,
    nome           VARCHAR(100) NOT NULL,
    papel          VARCHAR(10)  NOT NULL,
    CONSTRAINT pk_funcionario PRIMARY KEY (id_funcionario),
    CONSTRAINT ck_funcionario_papel CHECK (papel IN ('operador','gestor'))
);

CREATE TABLE CLIENTE (
    id_cliente          INT IDENTITY(1,1) NOT NULL,
    nome                VARCHAR(100) NOT NULL,
    telefone            VARCHAR(15)  NOT NULL,
    email               VARCHAR(150) NULL,
    telefone_verificado BIT          NOT NULL CONSTRAINT df_cliente_verificado DEFAULT 0,
    data_cadastro       DATETIME2    NOT NULL CONSTRAINT df_cliente_cadastro DEFAULT SYSDATETIME(),
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_telefone UNIQUE (telefone),
    CONSTRAINT ck_cliente_telefone CHECK (telefone NOT LIKE '%[^0-9]%' AND LEN(telefone) BETWEEN 10 AND 11),
    CONSTRAINT ck_cliente_email CHECK (email IS NULL OR email LIKE '%@%')
);

CREATE TABLE AREA_ENTREGA (
    id_area       INT IDENTITY(1,1) NOT NULL,
    bairro        VARCHAR(100)  NOT NULL,
    taxa_entrega  DECIMAL(10,2) NOT NULL,
    pedido_minimo DECIMAL(10,2) NOT NULL CONSTRAINT df_area_minimo DEFAULT 0,
    CONSTRAINT pk_area_entrega PRIMARY KEY (id_area),
    CONSTRAINT uq_area_bairro UNIQUE (bairro),
    CONSTRAINT ck_area_taxa CHECK (taxa_entrega >= 0),
    CONSTRAINT ck_area_minimo CHECK (pedido_minimo >= 0)
);

CREATE TABLE ENDERECO (
    id_endereco        INT IDENTITY(1,1) NOT NULL,
    id_cliente         INT           NOT NULL,
    rua                VARCHAR(150)  NOT NULL,
    numero             VARCHAR(10)   NOT NULL,
    complemento        VARCHAR(100)  NULL,
    bairro             VARCHAR(100)  NOT NULL,
    cep                VARCHAR(9)    NOT NULL,
    referencia         VARCHAR(150)  NULL,
    latitude           DECIMAL(9,6)  NOT NULL,
    longitude          DECIMAL(9,6)  NOT NULL,
    disponivel_entrega BIT           NOT NULL CONSTRAINT df_endereco_disp DEFAULT 1,
    CONSTRAINT pk_endereco PRIMARY KEY (id_endereco),
    -- exclusão em CASCATA: ao remover o cliente (LGPD), seus endereços são removidos
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTE (id_cliente) ON DELETE CASCADE,
    CONSTRAINT ck_endereco_cep CHECK (cep LIKE '[0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]'),
    CONSTRAINT ck_endereco_lat CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_endereco_lon CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE PRODUTO (
    id_produto                 INT IDENTITY(1,1) NOT NULL,
    nome                       VARCHAR(100)  NOT NULL,
    descricao                  VARCHAR(MAX)  NULL,
    preco                      DECIMAL(10,2) NOT NULL,
    categoria                  VARCHAR(50)   NOT NULL,
    foto_url                   VARCHAR(255)  NULL,
    status                     VARCHAR(10)   NOT NULL CONSTRAINT df_produto_status DEFAULT 'ativo',
    id_funcionario_alteracao   INT           NULL,
    data_alteracao             DATETIME2     NULL,
    CONSTRAINT pk_produto PRIMARY KEY (id_produto),
    CONSTRAINT fk_produto_funcionario FOREIGN KEY (id_funcionario_alteracao) REFERENCES FUNCIONARIO (id_funcionario),
    CONSTRAINT ck_produto_nome  CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ck_produto_preco CHECK (preco > 0),
    CONSTRAINT ck_produto_status CHECK (status IN ('ativo','pausado'))
);

CREATE TABLE HISTORICO_PRODUTO (
    id_historico_produto INT IDENTITY(1,1) NOT NULL,
    id_produto           INT           NOT NULL,
    id_funcionario       INT           NULL,
    preco_anterior       DECIMAL(10,2) NOT NULL,
    preco_novo           DECIMAL(10,2) NOT NULL,
    status_anterior      VARCHAR(10)   NOT NULL,
    status_novo          VARCHAR(10)   NOT NULL,
    data_alteracao       DATETIME2     NOT NULL CONSTRAINT df_histprod_data DEFAULT SYSDATETIME(),
    CONSTRAINT pk_historico_produto PRIMARY KEY (id_historico_produto),
    CONSTRAINT fk_histprod_produto FOREIGN KEY (id_produto) REFERENCES PRODUTO (id_produto) ON DELETE CASCADE,
    CONSTRAINT fk_histprod_func FOREIGN KEY (id_funcionario) REFERENCES FUNCIONARIO (id_funcionario)
);

CREATE TABLE GRUPO_OPCAO (
    id_grupo_opcao INT IDENTITY(1,1) NOT NULL,
    id_produto     INT         NOT NULL,
    nome           VARCHAR(50) NOT NULL,
    obrigatorio    BIT         NOT NULL CONSTRAINT df_grupo_obrig DEFAULT 0,
    min_selecoes   INT         NOT NULL CONSTRAINT df_grupo_min DEFAULT 0,
    max_selecoes   INT         NOT NULL CONSTRAINT df_grupo_max DEFAULT 1,
    CONSTRAINT pk_grupo_opcao PRIMARY KEY (id_grupo_opcao),
    CONSTRAINT fk_grupo_produto FOREIGN KEY (id_produto) REFERENCES PRODUTO (id_produto) ON DELETE CASCADE,
    CONSTRAINT ck_grupo_minmax CHECK (min_selecoes >= 0 AND max_selecoes >= 1 AND min_selecoes <= max_selecoes),
    CONSTRAINT ck_grupo_obrig CHECK (obrigatorio = 0 OR min_selecoes >= 1)
);

CREATE TABLE OPCAO (
    id_opcao        INT IDENTITY(1,1) NOT NULL,
    id_grupo_opcao  INT           NOT NULL,
    nome            VARCHAR(50)   NOT NULL,
    preco_adicional DECIMAL(10,2) NOT NULL CONSTRAINT df_opcao_preco DEFAULT 0,
    CONSTRAINT pk_opcao PRIMARY KEY (id_opcao),
    CONSTRAINT fk_opcao_grupo FOREIGN KEY (id_grupo_opcao) REFERENCES GRUPO_OPCAO (id_grupo_opcao) ON DELETE CASCADE,
    CONSTRAINT ck_opcao_preco CHECK (preco_adicional >= 0)
);

CREATE TABLE PEDIDO (
    -- AUTONUMERAÇÃO iniciando em 1000 com incremento de 10
    id_pedido           INT IDENTITY(1000,10) NOT NULL,
    id_cliente          INT           NOT NULL,
    id_endereco         INT           NULL,
    tipo_entrega        VARCHAR(10)   NOT NULL,
    data_pedido         DATETIME2     NOT NULL CONSTRAINT df_pedido_data DEFAULT SYSDATETIME(),
    status              VARCHAR(30)   NOT NULL CONSTRAINT df_pedido_status DEFAULT 'Pendente',
    subtotal_produtos   DECIMAL(10,2) NOT NULL CONSTRAINT df_pedido_sub DEFAULT 0,
    taxa_entrega        DECIMAL(10,2) NOT NULL CONSTRAINT df_pedido_taxa DEFAULT 0,
    desconto_cupom      DECIMAL(10,2) NOT NULL CONSTRAINT df_pedido_desc DEFAULT 0,
    valor_total         DECIMAL(10,2) NOT NULL CONSTRAINT df_pedido_total DEFAULT 0,
    motivo_cancelamento VARCHAR(200)  NULL,
    CONSTRAINT pk_pedido PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedido_cliente  FOREIGN KEY (id_cliente)  REFERENCES CLIENTE (id_cliente),
    CONSTRAINT fk_pedido_endereco FOREIGN KEY (id_endereco) REFERENCES ENDERECO (id_endereco),
    CONSTRAINT ck_pedido_tipo CHECK (tipo_entrega IN ('entrega','retirada')),
    CONSTRAINT ck_pedido_status CHECK (status IN ('Pendente','Pagamento aprovado','Aceito','Em preparo',
                                                  'Pronto/Saiu para entrega','Entregue','Cancelado')),
    CONSTRAINT ck_pedido_endereco_tipo CHECK ((tipo_entrega = 'entrega' AND id_endereco IS NOT NULL)
                                           OR (tipo_entrega = 'retirada' AND id_endereco IS NULL)),
    CONSTRAINT ck_pedido_valores CHECK (subtotal_produtos >= 0 AND taxa_entrega >= 0
                                        AND desconto_cupom >= 0 AND valor_total >= 0),
    CONSTRAINT ck_pedido_motivo CHECK (status <> 'Cancelado' OR motivo_cancelamento IS NOT NULL)
);

CREATE TABLE ITEM_PEDIDO (
    id_item        INT IDENTITY(1,1) NOT NULL,
    id_pedido      INT           NOT NULL,
    id_produto     INT           NOT NULL,
    quantidade     INT           NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,  -- preço congelado no momento da adição
    observacao     VARCHAR(200)  NULL,
    CONSTRAINT pk_item_pedido PRIMARY KEY (id_item),
    CONSTRAINT fk_item_pedido  FOREIGN KEY (id_pedido)  REFERENCES PEDIDO (id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (id_produto) REFERENCES PRODUTO (id_produto),
    CONSTRAINT ck_item_qtd   CHECK (quantidade > 0),
    CONSTRAINT ck_item_preco CHECK (preco_unitario >= 0)
);

CREATE TABLE ITEM_OPCAO (
    id_item_opcao   INT IDENTITY(1,1) NOT NULL,
    id_item         INT           NOT NULL,
    id_opcao        INT           NOT NULL,
    preco_adicional DECIMAL(10,2) NOT NULL CONSTRAINT df_itemopcao_preco DEFAULT 0, -- congelado
    CONSTRAINT pk_item_opcao PRIMARY KEY (id_item_opcao),
    CONSTRAINT fk_itemopcao_item  FOREIGN KEY (id_item)  REFERENCES ITEM_PEDIDO (id_item) ON DELETE CASCADE,
    CONSTRAINT fk_itemopcao_opcao FOREIGN KEY (id_opcao) REFERENCES OPCAO (id_opcao),
    CONSTRAINT uq_itemopcao UNIQUE (id_item, id_opcao),
    CONSTRAINT ck_itemopcao_preco CHECK (preco_adicional >= 0)
);

CREATE TABLE PAGAMENTO (
    id_pagamento         INT IDENTITY(1,1) NOT NULL,
    id_pedido            INT           NOT NULL,
    forma_pagamento      VARCHAR(10)   NOT NULL,
    status_pagamento     VARCHAR(10)   NOT NULL CONSTRAINT df_pag_status DEFAULT 'aguardando',
    valor                DECIMAL(10,2) NOT NULL,
    id_transacao_gateway VARCHAR(100)  NULL,   -- apenas referência ao gateway (sem dados de cartão)
    data_criacao         DATETIME2     NOT NULL CONSTRAINT df_pag_data DEFAULT SYSDATETIME(),
    CONSTRAINT pk_pagamento PRIMARY KEY (id_pagamento),
    CONSTRAINT fk_pagamento_pedido FOREIGN KEY (id_pedido) REFERENCES PEDIDO (id_pedido),
    CONSTRAINT ck_pag_forma  CHECK (forma_pagamento IN ('pix','cartao')),
    CONSTRAINT ck_pag_status CHECK (status_pagamento IN ('aguardando','aprovado','recusado','estornado')),
    CONSTRAINT ck_pag_valor  CHECK (valor > 0)
);
-- idempotência do webhook: a mesma transação do gateway não pode existir duas vezes
CREATE UNIQUE INDEX ux_pagamento_transacao ON PAGAMENTO (id_transacao_gateway) WHERE id_transacao_gateway IS NOT NULL;

CREATE TABLE AVALIACAO (
    id_avaliacao        INT IDENTITY(1,1) NOT NULL,
    id_pedido           INT           NOT NULL,
    nota                INT           NOT NULL,
    comentario          VARCHAR(MAX)  NULL,
    resposta_restaurante VARCHAR(MAX) NULL,
    data_avaliacao      DATETIME2     NOT NULL CONSTRAINT df_aval_data DEFAULT SYSDATETIME(),
    CONSTRAINT pk_avaliacao PRIMARY KEY (id_avaliacao),
    CONSTRAINT fk_avaliacao_pedido FOREIGN KEY (id_pedido) REFERENCES PEDIDO (id_pedido),
    CONSTRAINT uq_avaliacao_pedido UNIQUE (id_pedido),   -- uma avaliação por pedido
    CONSTRAINT ck_avaliacao_nota CHECK (nota BETWEEN 1 AND 5)
);

CREATE TABLE HISTORICO_STATUS (
    id_historico_status INT IDENTITY(1,1) NOT NULL,
    id_pedido           INT          NOT NULL,
    status_anterior     VARCHAR(30)  NOT NULL,
    status_novo         VARCHAR(30)  NOT NULL,
    data_alteracao      DATETIME2    NOT NULL CONSTRAINT df_histstatus_data DEFAULT SYSDATETIME(),
    id_funcionario      INT          NULL,    -- NULL = sistema/cliente
    motivo              VARCHAR(200) NULL,
    CONSTRAINT pk_historico_status PRIMARY KEY (id_historico_status),
    CONSTRAINT fk_histstatus_pedido FOREIGN KEY (id_pedido) REFERENCES PEDIDO (id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_histstatus_func   FOREIGN KEY (id_funcionario) REFERENCES FUNCIONARIO (id_funcionario)
);
GO
/* ============================================================
   Script 02 - Automação (FUNCTION, TRIGGER, STORED PROCEDURE) e INDEX
   ============================================================ */
USE DeliveryFacil;
GO

/* ---------- FUNCTIONS ---------- */
-- Taxa de entrega pelo bairro do endereço. Retorna NULL se o bairro estiver fora da área de cobertura.
CREATE OR ALTER FUNCTION dbo.fn_taxa_entrega (@id_endereco INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @taxa DECIMAL(10,2);
    SELECT @taxa = a.taxa_entrega
      FROM ENDERECO e
      INNER JOIN AREA_ENTREGA a ON a.bairro = e.bairro
     WHERE e.id_endereco = @id_endereco;
    RETURN @taxa;
END;
GO

-- Subtotal do pedido: SUM(quantidade * (preco_unitario + adicionais das opções))
CREATE OR ALTER FUNCTION dbo.fn_subtotal_pedido (@id_pedido INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @sub DECIMAL(10,2);
    SELECT @sub = ISNULL(SUM(i.quantidade * (i.preco_unitario + ISNULL(o.adicionais, 0))), 0)
      FROM ITEM_PEDIDO i
      OUTER APPLY (SELECT SUM(io.preco_adicional) AS adicionais
                     FROM ITEM_OPCAO io WHERE io.id_item = i.id_item) o
     WHERE i.id_pedido = @id_pedido;
    RETURN @sub;
END;
GO

/* ---------- TRIGGERS ---------- */
-- Recalcula subtotal e total do pedido quando itens são inseridos/alterados/removidos
-- valor_total = subtotal_produtos + taxa_entrega - desconto_cupom (nunca negativo)
CREATE OR ALTER TRIGGER dbo.trg_item_pedido_recalcula
ON ITEM_PEDIDO AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE p
       SET subtotal_produtos = s.sub,
           valor_total = CASE WHEN s.sub + p.taxa_entrega - p.desconto_cupom < 0 THEN 0
                              ELSE s.sub + p.taxa_entrega - p.desconto_cupom END
      FROM PEDIDO p
      CROSS APPLY (SELECT dbo.fn_subtotal_pedido(p.id_pedido) AS sub) s
     WHERE p.id_pedido IN (SELECT id_pedido FROM inserted UNION SELECT id_pedido FROM deleted);
END;
GO

-- Mesmo recálculo quando as opções/adicionais de um item mudam
CREATE OR ALTER TRIGGER dbo.trg_item_opcao_recalcula
ON ITEM_OPCAO AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE p
       SET subtotal_produtos = s.sub,
           valor_total = CASE WHEN s.sub + p.taxa_entrega - p.desconto_cupom < 0 THEN 0
                              ELSE s.sub + p.taxa_entrega - p.desconto_cupom END
      FROM PEDIDO p
      CROSS APPLY (SELECT dbo.fn_subtotal_pedido(p.id_pedido) AS sub) s
     WHERE p.id_pedido IN (SELECT ip.id_pedido FROM ITEM_PEDIDO ip
                            WHERE ip.id_item IN (SELECT id_item FROM inserted UNION SELECT id_item FROM deleted));
END;
GO

-- Auditoria de preço/status do produto; só o papel "gestor" pode alterar (Estória 3)
CREATE OR ALTER TRIGGER dbo.trg_produto_auditoria
ON PRODUTO AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN deleted d ON d.id_produto = i.id_produto
                WHERE (i.preco <> d.preco OR i.status <> d.status)
                  AND NOT EXISTS (SELECT 1 FROM FUNCIONARIO f
                                   WHERE f.id_funcionario = i.id_funcionario_alteracao AND f.papel = 'gestor'))
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50010, N'Apenas funcionário com papel gestor pode alterar preço ou status do produto.', 1;
    END;

    INSERT INTO HISTORICO_PRODUTO (id_produto, id_funcionario, preco_anterior, preco_novo, status_anterior, status_novo)
    SELECT i.id_produto, i.id_funcionario_alteracao, d.preco, i.preco, d.status, i.status
      FROM inserted i JOIN deleted d ON d.id_produto = i.id_produto
     WHERE i.preco <> d.preco OR i.status <> d.status;
END;
GO

-- Avaliação só é permitida para pedido "Entregue" (Estória 10)
CREATE OR ALTER TRIGGER dbo.trg_avaliacao_valida_entrega
ON AVALIACAO AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN PEDIDO p ON p.id_pedido = i.id_pedido WHERE p.status <> 'Entregue')
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50020, N'Avaliação só é permitida para pedidos com status Entregue.', 1;
    END;
END;
GO

/* ---------- STORED PROCEDURES ---------- */
-- Atualiza o status respeitando o fluxo, exige motivo no cancelamento e grava o histórico
CREATE OR ALTER PROCEDURE dbo.sp_atualizar_status_pedido
    @id_pedido      INT,
    @novo_status    VARCHAR(30),
    @id_funcionario INT = NULL,          -- NULL = cliente/sistema
    @motivo         VARCHAR(200) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @atual VARCHAR(30);
    BEGIN TRY
        BEGIN TRAN;
        SELECT @atual = status FROM PEDIDO WITH (UPDLOCK, ROWLOCK) WHERE id_pedido = @id_pedido;

        IF @atual IS NULL
            THROW 50001, N'Pedido não encontrado.', 1;
        IF @id_funcionario IS NOT NULL
           AND NOT EXISTS (SELECT 1 FROM FUNCIONARIO WHERE id_funcionario = @id_funcionario)
            THROW 50002, N'Funcionário não encontrado.', 1;

        IF @novo_status = 'Cancelado'
        BEGIN
            IF @atual IN ('Entregue','Cancelado')
                THROW 50003, N'Pedido não pode mais ser cancelado.', 1;
            IF NULLIF(LTRIM(RTRIM(@motivo)), '') IS NULL
                THROW 50004, N'Motivo é obrigatório para cancelamento.', 1;
            IF @id_funcionario IS NULL AND @atual <> 'Pendente'
                THROW 50005, N'Cliente só pode cancelar enquanto o pedido estiver Pendente.', 1;
        END
        ELSE IF NOT (   (@atual = 'Pendente'                 AND @novo_status = 'Pagamento aprovado')
                     OR (@atual = 'Pagamento aprovado'       AND @novo_status = 'Aceito')
                     OR (@atual = 'Aceito'                   AND @novo_status = 'Em preparo')
                     OR (@atual = 'Em preparo'               AND @novo_status = 'Pronto/Saiu para entrega')
                     OR (@atual = 'Pronto/Saiu para entrega' AND @novo_status = 'Entregue'))
            THROW 50006, N'Transição de status inválida para o fluxo do pedido.', 1;

        UPDATE PEDIDO
           SET status = @novo_status,
               motivo_cancelamento = CASE WHEN @novo_status = 'Cancelado' THEN @motivo ELSE motivo_cancelamento END
         WHERE id_pedido = @id_pedido;

        INSERT INTO HISTORICO_STATUS (id_pedido, status_anterior, status_novo, id_funcionario, motivo)
        VALUES (@id_pedido, @atual, @novo_status, @id_funcionario, @motivo);
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

-- Cancela pedidos cujo Pix está pendente há mais de N minutos (padrão 15) - agendar via SQL Agent
CREATE OR ALTER PROCEDURE dbo.sp_cancelar_pix_expirados
    @minutos INT = 15
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @ids TABLE (id_pedido INT);
    BEGIN TRY
        BEGIN TRAN;
        UPDATE pg SET status_pagamento = 'recusado'
        OUTPUT inserted.id_pedido INTO @ids (id_pedido)
          FROM PAGAMENTO pg
          INNER JOIN PEDIDO p ON p.id_pedido = pg.id_pedido
         WHERE pg.forma_pagamento = 'pix' AND pg.status_pagamento = 'aguardando'
           AND p.status = 'Pendente'
           AND pg.data_criacao < DATEADD(MINUTE, -@minutos, SYSDATETIME());

        UPDATE p SET status = 'Cancelado', motivo_cancelamento = N'Pix não pago no prazo'
          FROM PEDIDO p INNER JOIN @ids i ON i.id_pedido = p.id_pedido;

        INSERT INTO HISTORICO_STATUS (id_pedido, status_anterior, status_novo, motivo)
        SELECT id_pedido, 'Pendente', 'Cancelado', N'Pix não pago no prazo' FROM @ids;
        COMMIT;
        SELECT COUNT(*) AS pedidos_cancelados FROM @ids;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

/* ---------- ÍNDICES (PEDIDO é a tabela de maior volume de consulta) ---------- */
-- histórico/consulta de pedidos por cliente, mais recentes primeiro
CREATE NONCLUSTERED INDEX ix_pedido_cliente_data
    ON PEDIDO (id_cliente, data_pedido DESC) INCLUDE (status, valor_total);
-- painel do restaurante: pedidos por status
CREATE NONCLUSTERED INDEX ix_pedido_status_data
    ON PEDIDO (status, data_pedido) INCLUDE (id_cliente, valor_total);
-- junção itens -> pedido
CREATE NONCLUSTERED INDEX ix_item_pedido_pedido
    ON ITEM_PEDIDO (id_pedido) INCLUDE (id_produto, quantidade, preco_unitario);
GO
/* ============================================================
   Script 03 - Alimentação das tabelas (mínimo 3 registros por tabela)
   Execute após 01 e 02. Os totais dos pedidos são calculados pelos TRIGGERS.
   Os IDs de PEDIDO (1000, 1010, ...) dependem do banco recém-criado pelo script 01.
   ============================================================ */
USE DeliveryFacil;
GO

INSERT INTO FUNCIONARIO (nome, papel) VALUES
(N'Marcos Souza', 'gestor'), (N'Ana Paula Ribeiro', 'operador'), (N'Carlos Menezes', 'operador');

INSERT INTO AREA_ENTREGA (bairro, taxa_entrega, pedido_minimo) VALUES
(N'Centro', 5.00, 20.00), (N'Jardim das Flores', 8.00, 25.00),
(N'Vila Nova', 10.00, 30.00), (N'Santa Maria', 12.00, 35.00);

INSERT INTO CLIENTE (nome, telefone, email, telefone_verificado) VALUES
(N'Bruno Nicácio de Lima', '11987654321', 'bruno@email.com', 1),
(N'Thayanne Fernanda',     '11976543210', NULL,              1),
(N'Fernando Alves',        '11965432109', 'fernando@email.com', 1),
(N'Wellington Muniz',      '11954321098', NULL,              0);

INSERT INTO ENDERECO (id_cliente, rua, numero, complemento, bairro, cep, referencia, latitude, longitude, disponivel_entrega) VALUES
(1, N'Rua das Acácias', '120', N'Apto 31', N'Centro',            '01001-000', N'Próximo à praça',   -23.550520, -46.633308, 1),
(2, N'Avenida Brasil',  '450', NULL,       N'Jardim das Flores', '01310-100', N'Em frente ao mercado', -23.561414, -46.655881, 1),
(3, N'Rua do Sol',      '88',  N'Casa 2',  N'Vila Nova',         '04567-020', NULL,                 -23.601100, -46.672300, 1),
(4, N'Estrada Velha',   '900', NULL,       N'Bairro Distante',   '09999-000', N'Sítio',             -23.900000, -46.900000, 0);

INSERT INTO PRODUTO (nome, descricao, preco, categoria, foto_url, status) VALUES
(N'X-Burger Artesanal', N'Pão brioche, blend 160g, queijo e molho da casa', 28.90, N'Lanches',    'https://cdn.exemplo.com/xburger.jpg', 'ativo'),
(N'Pizza Calabresa',    N'Molho de tomate, calabresa e cebola',             49.90, N'Pizzas',     'https://cdn.exemplo.com/pizza.jpg',   'ativo'),
(N'Suco de Laranja',    N'Suco natural',                                     9.50, N'Bebidas',    NULL,                                   'ativo'),
(N'Brownie com Sorvete',N'Brownie quente com sorvete de creme',             16.00, N'Sobremesas', NULL,                                   'pausado');

INSERT INTO HISTORICO_PRODUTO (id_produto, id_funcionario, preco_anterior, preco_novo, status_anterior, status_novo, data_alteracao) VALUES
(3, 1,  8.50,  9.50, 'ativo', 'ativo',   '2026-11-01T10:00:00'),
(2, 1, 45.90, 49.90, 'ativo', 'ativo',   '2026-11-05T09:30:00'),
(4, 1, 16.00, 16.00, 'ativo', 'pausado', '2026-11-10T18:00:00');

INSERT INTO GRUPO_OPCAO (id_produto, nome, obrigatorio, min_selecoes, max_selecoes) VALUES
(2, N'Tamanho',    1, 1, 1),   -- grupo 1: pizza
(1, N'Adicionais', 0, 0, 3),   -- grupo 2: burger
(3, N'Tamanho',    1, 1, 1);   -- grupo 3: suco

INSERT INTO OPCAO (id_grupo_opcao, nome, preco_adicional) VALUES
(1, N'Média', 0.00), (1, N'Grande', 10.00),
(2, N'Bacon extra', 4.00), (2, N'Queijo extra', 3.50),
(3, N'300 ml', 0.00), (3, N'500 ml', 3.00);

-- id_pedido gerado: 1000, 1010, 1020, 1030, 1040 (IDENTITY 1000,10)
INSERT INTO PEDIDO (id_cliente, id_endereco, tipo_entrega, data_pedido, status, taxa_entrega, desconto_cupom) VALUES
(1, 1,    'entrega',  '2026-11-20T19:30:00', 'Entregue',   dbo.fn_taxa_entrega(1), 0),
(2, 2,    'entrega',  '2026-11-21T20:10:00', 'Entregue',   dbo.fn_taxa_entrega(2), 5.00),
(3, NULL, 'retirada', '2026-11-22T12:45:00', 'Entregue',   0, 0),
(1, 1,    'entrega',  '2026-11-24T11:50:00', 'Em preparo', dbo.fn_taxa_entrega(1), 0),
(3, 3,    'entrega',  SYSDATETIME(),         'Pendente',   dbo.fn_taxa_entrega(3), 0);

INSERT INTO ITEM_PEDIDO (id_pedido, id_produto, quantidade, preco_unitario, observacao) VALUES
(1000, 1, 2, 28.90, N'sem cebola'),
(1000, 3, 2,  9.50, NULL),
(1010, 2, 1, 49.90, N'bem assada'),
(1020, 1, 1, 28.90, NULL),
(1030, 2, 1, 49.90, NULL),
(1040, 1, 1, 28.90, NULL);

INSERT INTO ITEM_OPCAO (id_item, id_opcao, preco_adicional) VALUES
(1, 3,  4.00),   -- burger do pedido 1000 + bacon extra
(2, 6,  3.00),   -- suco 500 ml do pedido 1000
(3, 2, 10.00),   -- pizza grande do pedido 1010
(5, 1,  0.00);   -- pizza média do pedido 1030

INSERT INTO PAGAMENTO (id_pedido, forma_pagamento, status_pagamento, valor, id_transacao_gateway)
SELECT p.id_pedido, v.forma, v.st, p.valor_total, v.tx
  FROM (VALUES (1000,'pix','aprovado','GW-PIX-0001'), (1010,'cartao','aprovado','GW-CRD-0002'),
               (1020,'pix','aprovado','GW-PIX-0003'), (1030,'cartao','aprovado','GW-CRD-0004'),
               (1040,'pix','aguardando','GW-PIX-0005')) AS v(id_pedido, forma, st, tx)
  JOIN PEDIDO p ON p.id_pedido = v.id_pedido;

INSERT INTO AVALIACAO (id_pedido, nota, comentario, resposta_restaurante) VALUES
(1000, 5, N'Chegou quente e antes do prazo!', N'Obrigado, Bruno! Volte sempre.'),
(1010, 4, N'Pizza ótima, só demorou um pouco.', NULL),
(1020, 5, NULL, NULL);

INSERT INTO HISTORICO_STATUS (id_pedido, status_anterior, status_novo, id_funcionario, data_alteracao) VALUES
(1000, 'Pendente',                 'Pagamento aprovado',       NULL, '2026-11-20T19:31:00'),
(1000, 'Pagamento aprovado',       'Aceito',                   2,    '2026-11-20T19:32:00'),
(1000, 'Aceito',                   'Em preparo',               2,    '2026-11-20T19:35:00'),
(1000, 'Em preparo',               'Pronto/Saiu para entrega', 3,    '2026-11-20T19:55:00'),
(1000, 'Pronto/Saiu para entrega', 'Entregue',                 3,    '2026-11-20T20:15:00');

-- Demonstração do trigger de auditoria + preço congelado nos itens:
-- o preço do X-Burger sobe, mas os itens já criados continuam com 28,90.
UPDATE PRODUTO SET preco = 29.90, id_funcionario_alteracao = 1, data_alteracao = SYSDATETIME() WHERE id_produto = 1;
GO
/* ============================================================
   Script 04 - Consultas (ALIAS, 2+ JOINs, funções de agregação) e teste das rotinas
   ============================================================ */
USE DeliveryFacil;
GO

-- C1. Pedidos com cliente, bairro e pagamento (3 JOINs, ALIAS)
SELECT p.id_pedido, c.nome AS cliente, e.bairro, p.tipo_entrega, pg.forma_pagamento,
       pg.status_pagamento, p.status AS status_pedido, p.valor_total
  FROM PEDIDO p
  INNER JOIN CLIENTE   c  ON c.id_cliente  = p.id_cliente
  LEFT  JOIN ENDERECO  e  ON e.id_endereco = p.id_endereco
  INNER JOIN PAGAMENTO pg ON pg.id_pedido  = p.id_pedido
 ORDER BY p.data_pedido DESC;

-- C2. Faturamento por forma de pagamento: COUNT, SUM, AVG, MAX, MIN
SELECT pg.forma_pagamento, COUNT(*) AS qtd_pagamentos, SUM(pg.valor) AS faturamento,
       AVG(pg.valor) AS ticket_medio, MAX(pg.valor) AS maior_pedido, MIN(pg.valor) AS menor_pedido
  FROM PAGAMENTO pg
 WHERE pg.status_pagamento = 'aprovado'
 GROUP BY pg.forma_pagamento;

-- C3. Produtos mais vendidos (3 tabelas) - ignora pedidos cancelados
SELECT pr.nome AS produto, pr.categoria, SUM(i.quantidade) AS unidades_vendidas,
       SUM(i.quantidade * i.preco_unitario) AS receita_bruta
  FROM ITEM_PEDIDO i
  INNER JOIN PRODUTO pr ON pr.id_produto = i.id_produto
  INNER JOIN PEDIDO  p  ON p.id_pedido   = i.id_pedido
 WHERE p.status <> 'Cancelado'
 GROUP BY pr.nome, pr.categoria
 ORDER BY unidades_vendidas DESC;

-- C4. Nota média das avaliações por bairro (AVG, COUNT, HAVING)
SELECT e.bairro, COUNT(a.id_avaliacao) AS qtd_avaliacoes,
       AVG(CAST(a.nota AS DECIMAL(3,2))) AS nota_media
  FROM AVALIACAO a
  INNER JOIN PEDIDO   p ON p.id_pedido   = a.id_pedido
  INNER JOIN ENDERECO e ON e.id_endereco = p.id_endereco
 GROUP BY e.bairro
HAVING COUNT(a.id_avaliacao) >= 1;

-- C5. Clientes que ainda não fizeram pedido (LEFT JOIN)
SELECT c.id_cliente, c.nome, c.telefone, c.telefone_verificado
  FROM CLIENTE c
  LEFT JOIN PEDIDO p ON p.id_cliente = c.id_cliente
 WHERE p.id_pedido IS NULL;

-- C6. Gasto total e ticket médio por cliente (pedidos não cancelados)
SELECT c.nome, COUNT(p.id_pedido) AS pedidos, SUM(p.valor_total) AS total_gasto,
       AVG(p.valor_total) AS ticket_medio
  FROM CLIENTE c
  INNER JOIN PEDIDO p ON p.id_cliente = c.id_cliente
 WHERE p.status <> 'Cancelado'
 GROUP BY c.nome
 ORDER BY total_gasto DESC;

-- C7. Itens com adicionais (4 JOINs): mostra o preço congelado x preço atual do produto
SELECT i.id_pedido, pr.nome AS produto, i.preco_unitario AS preco_congelado, pr.preco AS preco_atual,
       o.nome AS adicional, io.preco_adicional
  FROM ITEM_PEDIDO i
  INNER JOIN PRODUTO    pr ON pr.id_produto = i.id_produto
  INNER JOIN ITEM_OPCAO io ON io.id_item    = i.id_item
  INNER JOIN OPCAO      o  ON o.id_opcao    = io.id_opcao;

-- C8. Auditoria de produtos (gerada por TRIGGER e carga inicial)
SELECT h.id_produto, pr.nome, h.preco_anterior, h.preco_novo, h.status_anterior, h.status_novo,
       f.nome AS alterado_por, h.data_alteracao
  FROM HISTORICO_PRODUTO h
  INNER JOIN PRODUTO pr ON pr.id_produto = h.id_produto
  LEFT  JOIN FUNCIONARIO f ON f.id_funcionario = h.id_funcionario
 ORDER BY h.data_alteracao;

/* ---------- Testes das rotinas ---------- */
-- Função: taxa por endereço (NULL = fora da área)
SELECT dbo.fn_taxa_entrega(1) AS taxa_centro, dbo.fn_taxa_entrega(4) AS taxa_fora_area;
-- Função + trigger: totais calculados automaticamente
SELECT id_pedido, subtotal_produtos, taxa_entrega, desconto_cupom, valor_total, dbo.fn_subtotal_pedido(id_pedido) AS conferencia
  FROM PEDIDO;
-- Procedure: avança o pedido 1030 de "Em preparo" para "Pronto/Saiu para entrega" (operador 3)
EXEC dbo.sp_atualizar_status_pedido @id_pedido = 1030, @novo_status = 'Pronto/Saiu para entrega', @id_funcionario = 3;
SELECT * FROM HISTORICO_STATUS WHERE id_pedido = 1030;
-- Procedure: expiração de Pix (nenhum pendente há mais de 15 min na carga inicial)
EXEC dbo.sp_cancelar_pix_expirados @minutos = 15;
/* Testes de erro (descomente para ver as exceções):
EXEC dbo.sp_atualizar_status_pedido 1040, 'Entregue', 2;                 -- transição inválida
EXEC dbo.sp_atualizar_status_pedido 1040, 'Cancelado', 2, NULL;           -- sem motivo
INSERT INTO AVALIACAO (id_pedido, nota) VALUES (1040, 5);                 -- pedido não entregue
UPDATE PRODUTO SET preco = 1 WHERE id_produto = 2;                        -- sem gestor identificado
*/
GO
