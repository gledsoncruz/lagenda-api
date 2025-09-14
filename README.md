# Getting Started

Toda vez que o projeto for alterado, rodar os comandos:

```
docker-compose down
docker-compose up --build --force-recreate
```

Para enviar para o docker hub:

```
docker build -t gledsoncruz/lagenda-app .
docker push gledsoncruz/lagenda-app:latest
```