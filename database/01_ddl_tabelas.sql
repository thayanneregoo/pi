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
