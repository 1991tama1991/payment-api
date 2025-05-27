INSERT INTO accounts (id, balance)
VALUES ('11111111-1111-1111-1111-111111111111', 500.0),
       ('22222222-2222-2222-2222-222222222222', 1000.0);

INSERT INTO payments (id, amount, recipient, sender, date_time)
VALUES ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 100.0, '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111', NOW()),
       ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 200.0, '11111111-1111-1111-1111-111111111111',
        '22222222-2222-2222-2222-222222222222', NOW());
