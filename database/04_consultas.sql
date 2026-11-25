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
