## 部署指南

### 依赖服务
docker run --name sso_mysql --restart always  --env-file ./env.list -p 127.0.0.1:3306:3306 -d mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
docker run --name sso_redis --restart always -p 127.0.0.1:6379:6379 -d redis redis-server

初始化数据
docker exec -i sso_mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD"  -D oauth' < ./schema.sql