#!/bin/bash
host="$1"
shift
cmd="$@"

until pg_isready -h "$host"; do
  >&2 echo "PostgreSQL ainda não está disponível - aguardando"
  sleep 1
done

>&2 echo "PostgreSQL está pronto - iniciando a aplicação"
exec $cmd
