#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
backend_dir="$(cd -- "$script_dir/.." && pwd)"
zk_home="${ZOOKEEPER_HOME:-$backend_dir/local-services/apache-zookeeper-3.9.7-bin}"
if [[ ! -f "$zk_home/bin/zkServer.sh" ]]; then
    echo "ZooKeeper not found. See backend/local-services/README.md or set ZOOKEEPER_HOME." >&2
    exit 1
fi
zk_home="$(cd -- "$zk_home" && pwd)"
# The shared config uses a relative dataDir, so always anchor the working directory.
# The distribution must be an immediate child so ../../datas stays valid.
if [[ "$(dirname -- "$zk_home")" != "$backend_dir/local-services" ]]; then
    echo "Place the ZooKeeper distribution under backend/local-services (see README)." >&2
    exit 1
fi
case "${1:-status}" in
    start|stop|status|start-foreground|print-cmd) ;;
    *) echo "Usage: bash zookeeper.sh {start|stop|status|start-foreground|print-cmd}" >&2; exit 2 ;;
esac
export ZOO_LOG_DIR="$backend_dir/datas/zookeeper-logs"
export ZOOPIDFILE="$backend_dir/datas/zookeeper-data/zookeeper_server.pid"
cd -- "$zk_home"
exec bash bin/zkServer.sh --config "$backend_dir/config/zookeeper" "${1:-status}"
