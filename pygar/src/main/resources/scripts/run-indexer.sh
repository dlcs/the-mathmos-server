#!/bin/bash
ANNOTATION_INDEXER_HOME=/home/digirati/annotation-search-server/dev/annotation-indexer/pygar
java -cp $ANNOTATION_INDEXER_HOME/pygar.jar:$ANNOTATION_INDEXER_HOME/lib/*:$ANNOTATION_INDEXER_HOME/config/:$ANNOTATION_INDEXER_HOME/. com.digirati.pygar.mapping.IndexAWSMessages --Dpygar-aws-consumer.properties=file:/home/digirati/annotation-search-server/dev/annotation-indexer/pygar-aws-consumer.properties


