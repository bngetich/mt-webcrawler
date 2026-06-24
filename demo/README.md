# Local Crawler Demo

This demo uses a controlled local website so the crawler can prove:

- it starts from a seed URL
- it discovers links from fetched pages
- it skips URLs disallowed by `robots.txt`
- it stores frontier and visited state in Redis
- it prints basic crawler metrics

## 1. Start Redis

Start Redis on port `6379`.

If Redis is installed locally:

```powershell
redis-server
```

If Redis is running in Docker:

```powershell
docker start <redis-container-name>
```

## 2. Clear Redis

For a clean local demo run:

```powershell
redis-cli FLUSHDB
```

This removes old frontier, visited, retry, dead-letter, and robots cache data from previous runs.

## 3. Start The Demo Website

In a separate terminal:

```powershell
node demo/demo-site.js
```

Expected output:

```text
Demo site running at http://localhost:8081/
robots.txt available at http://localhost:8081/robots.txt
```

The demo site exposes:

```text
http://localhost:8081/
http://localhost:8081/page-1
http://localhost:8081/page-2
http://localhost:8081/private/secret
http://localhost:8081/robots.txt
```

Its `robots.txt` says:

```text
User-agent: *
Disallow: /private
Crawl-delay: 2
```

## 4. Build The Crawler

```powershell
mvn package -DskipTests
```

## 5. Run The Crawler

In PowerShell, quote each `-D` system property:

```powershell
java '-Dfrontier.type=partitioned-redis' '-Dfrontier.partitions=3' '-Dfrontier.assignedPartition=0' '-Dvisited.type=redis' -jar target/mtWebCrawler-1.0-SNAPSHOT.jar http://localhost:8081/
```

## Expected Result

The crawler should save the allowed pages:

```text
Saved: http://localhost:8081/
Saved: http://localhost:8081/page-1
Saved: http://localhost:8081/page-2
```

It should not save:

```text
http://localhost:8081/private/secret
```

because `robots.txt` disallows `/private`.

The metrics output should look similar to:

```text
=== CRAWLER METRICS ===
Pages fetched: 3
Fetch failures: 0
Retries scheduled: 0
Dead-lettered: 0
Frontier size: 0
=======================
```

## Interview Explanation

This demo shows a distributed-crawler style design locally. Redis stores frontier and visited state, the crawler reads from its assigned frontier partition, `robots.txt` rules are fetched and cached, disallowed URLs are skipped, and basic metrics show crawl progress and failures.
