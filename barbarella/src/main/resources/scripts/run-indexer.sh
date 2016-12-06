#!/bin/bash
TEXT_INDEXER_HOME=/home/digirati/annotation-search-server/dev/text-indexer/digirati-text-indexer
java -cp $TEXT_INDEXER_HOME/barbarella.jar:$TEXT_INDEXER_HOME/lib/*:$TEXT_INDEXER_HOME/config/:$TEXT_INDEXER_HOME/. com.digirati.barbarella.mapping.IndexAWSMessages


