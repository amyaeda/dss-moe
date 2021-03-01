use dssmoe;

INSERT INTO `role` VALUES (1, 'USER');
INSERT INTO `role` VALUES (2, 'ADMIN');

INSERT INTO `user`(`user_id`,`active`,`user_name`,`last_name`,`first_name`,`password`) VALUES(1, 1, 'paridah', 'ida', 'Jakpar', '$2y$12$YpB8nEwMqkDLjD8bj46sreiaFwXNVvzj/9hZmeUWx2wZlM8ofOUoO');

INSERT INTO `user_role`(`user_id`,`role_id`) VALUES(1, 1);
INSERT INTO `user_role`(`user_id`,`role_id`) VALUES(1, 2);