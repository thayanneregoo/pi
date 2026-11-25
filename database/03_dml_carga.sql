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
