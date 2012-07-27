ALTER TABLE iconomy DROP COLUMN id;
ALTER TABLE iconomy DROP COLUMN status;
ALTER TABLE iconomy CHANGE username name varchar(16);
ALTER TABLE iconomy CHANGE balance money double;

RENAME TABLE iconomy TO fe_accounts;