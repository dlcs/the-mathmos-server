#!/bin/bash
ANNOTATION_INDEXER_HOME=/home/digirati/annotation-search-server/dev/annotation-indexer/digirati-annotation-indexer
java -cp $ANNOTATION_INDEXER_HOME/pygar.jar:$ANNOTATION_INDEXER_HOME/lib/*:$ANNOTATION_INDEXER_HOME/config/:$ANNOTATION_INDEXER_HOME/. com.digirati.pygar.mapping.IndexAWSMessages


