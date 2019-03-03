#!/usr/bin/env python

from __future__ import division

import sys
import json
from boto import kinesis
import time
import argparse
import base64


def get_args():
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--datacenter', '-d', type=str, default='us-east-1')
    parser.add_argument('--shardid', '-s', type=str,
                        default='shardId-000000000000')
    parser.add_argument('--name', '-n', type=str, default='endo-invalid-stream')
    parser.add_argument('--iterator', '-i', type=str, default='TRIM_HORIZON')
    parser.add_argument('--interval', type=float, default=1)
    parser.add_argument('--limit', type=int, default=500)
    parser.add_argument('--key', type=str, default=None)
    parser.add_argument('--keysecret', type=str, default=None)

    return parser.parse_args()


def main(args):
    if args.key is None or args.keysecret is None:
        kin = kinesis.connect_to_region(args.datacenter)
    else:
        kin = kinesis.connect_to_region(args.datacenter, aws_access_key_id=args.key,
                                        aws_secret_access_key=args.keysecret)

    shard_id = args.shardid
    shard_it = kin.get_shard_iterator(args.name, shard_id, args.iterator)['ShardIterator']

    while True:
        out = kin.get_records(shard_it, limit=args.limit)
        for o in out['Records']:
            print(o['Data'])
            jdat = json.loads(o['Data'])
            sys.stdout.write('%s\n' % json.dumps(jdat))
            sys.stdout.flush()
        shard_it = out['NextShardIterator']
        time.sleep(args.interval)


if __name__ == '__main__':
    main(get_args())
