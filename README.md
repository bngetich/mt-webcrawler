# Multi-Threaded Web Crawler

This repo explores the design and evolution of a web crawler,
starting from a single node multithreaded worker and progressing toward
a distributed crawler architecture.

The focus of this project is **system design and architecture**, not
just crawling functionality.

------------------------------------------------------------------------

## Overview

The crawler processes pages using a pipeline:

URL → Fetch → Parse → Extract Links → Store

Multiple worker threads run this pipeline concurrently using a shared
queue and visited set to avoid duplicate processing.

Core logic can be found in:

-   `Main.java`
-   `Crawler.java`

------------------------------------------------------------------------

## Design Goals

This project focuses on:

-   clean **object-oriented design**
-   clear **separation of responsibilities**
-   preparing the crawler architecture to evolve from a **single node**
    to a **distributed system**

Key components include:

-   `Crawler` -- orchestrates the crawling process
-   `ComponentFactory` -- centralizes crawler dependency creation
-   `WorkerTask` -- executes the crawl pipeline
-   `Fetcher` -- retrieves web pages
-   `Parser` -- extracts text and links
-   `Storage` -- handles output
-   `VisitedTracker` -- prevents duplicate crawling

------------------------------------------------------------------------

## Current Architecture (LLD)

The current implementation represents a **single-node crawler worker**.

Multiple threads consume URLs from a shared queue and process them
concurrently.

![Crawler LLD Diagram](design/01_crawler_lld.png)

------------------------------------------------------------------------

## Future Direction

The architecture is designed to evolve toward a distributed crawler:

1.  Single-node multithreaded worker (current)
2.  Component abstraction and factories
3.  Pluggable frontier (queue abstraction)
4.  Distributed crawler nodes
5.  Distributed storage and deduplication

The goal is to gradually move from a **local crawler worker** to a
**scalable distributed crawling system**.

------------------------------------------------------------------------

## Running the Project

Compile and run the crawler:

mvn clean compile
mvn exec:java -Dexec.mainClass="Main"

The crawler will start from a seed URL and process pages using multiple
worker threads.

