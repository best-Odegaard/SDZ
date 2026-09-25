-- =============================================================
-- 赛搭子管理端 菜单与权限 SQL
-- 在 ry_20250522.sql 之后执行：
--   mysql -u root -p saidazi < sql/saidazi_menu.sql
-- 菜单ID段：2100+（若依 sys_menu auto_increment=2000，使用 2100 避免冲突）
-- =============================================================
USE `saidazi`;

-- 一级目录：赛搭子管理
insert into sys_menu values('2100', '赛搭子管理', '0', '5', 'saidazi', null, '', '', 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), '', null, '赛搭子业务管理目录');

-- 二级菜单：数据概览
insert into sys_menu values('2101', '数据概览', '2100', '1', 'dashboard', 'saidazi/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'saidazi:dashboard:view', 'dashboard', 'admin', sysdate(), '', null, '赛搭子数据概览');

-- 二级菜单：赛事管理
insert into sys_menu values('2110', '赛事管理', '2100', '2', 'competition', 'saidazi/competition/index', '', '', 1, 0, 'C', '0', '0', 'saidazi:competition:list', 'date', 'admin', sysdate(), '', null, '赛事管理菜单');
-- 赛事管理按钮
insert into sys_menu values('2111', '赛事查询', '2110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:competition:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2112', '赛事新增', '2110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:competition:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2113', '赛事修改', '2110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:competition:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2114', '赛事删除', '2110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:competition:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2115', '赛事导出', '2110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:competition:export', '#', 'admin', sysdate(), '', null, '');

-- 二级菜单：往届成果
insert into sys_menu values('2120', '往届成果', '2100', '3', 'achievement', 'saidazi/achievement/index', '', '', 1, 0, 'C', '0', '0', 'saidazi:achievement:list', 'education', 'admin', sysdate(), '', null, '往届成果管理菜单');
insert into sys_menu values('2121', '成果查询', '2120', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:achievement:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2122', '成果新增', '2120', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:achievement:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2123', '成果修改', '2120', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:achievement:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2124', '成果删除', '2120', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:achievement:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2125', '成果导出', '2120', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'saidazi:achievement:export', '#', 'admin', sysdate(), '', null, '');

-- 把所有赛搭子菜单授权给超管角色(role_id=1)
insert into sys_role_menu values ('1', '2100');
insert into sys_role_menu values ('1', '2101');
insert into sys_role_menu values ('1', '2110');
insert into sys_role_menu values ('1', '2111');
insert into sys_role_menu values ('1', '2112');
insert into sys_role_menu values ('1', '2113');
insert into sys_role_menu values ('1', '2114');
insert into sys_role_menu values ('1', '2115');
insert into sys_role_menu values ('1', '2120');
insert into sys_role_menu values ('1', '2121');
insert into sys_role_menu values ('1', '2122');
insert into sys_role_menu values ('1', '2123');
insert into sys_role_menu values ('1', '2124');
insert into sys_role_menu values ('1', '2125');