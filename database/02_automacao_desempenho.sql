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
