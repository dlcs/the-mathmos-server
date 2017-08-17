# Mathmos API

The Mathmos is an entry point for searching annotations and text images. These are indexed into elasticsearch using separate indexes and mappings. For annotations we will have indexed any annotation that is uploaded into the Elucidate server. For text, we will have indexed any text that has been fed to us by Starsky. Text searching requires further lookups from a coordinate service; given a text phrase and its position in an indexed text item, give us the xywh coordinates for that phrase in the corresponding image, possibly scaled with width and height input parameters). These are then made into annotations on the fly when searched, and in addition to resources, also yield hits.

## Entry points:
There are a number of entry points into the Mathmos. These are for search and autocomplete, both for text and annotations alone and for both together, and for **w3c** and **oa**.

These are:

###  Search

​

#### Annotations only:

    /search/oa/search
    /search/w3c/search

#### Text only:

    /search/oa/text/search
    /search/w3c/text/search

#### Both text and annotations:

    /search/search/oa
    /search/search/w3c

###  Autocomplete



#### Annotations only:

```
/search/oa/autocomplete
/search/w3c/autocomplete
```

#### Text only:

```
/search/oa/text/autocomplete
/search/w3c/text/autocomplete
```

#### Both text and annotations:

```
/search/autocomplete/oa
/search/autocomplete/w3c
```



## Search Request:

### Parameters:

The following parameters (inline with http://iiif.io/api/search/1.0/#query-parameters) are allowed in a mathmos search:

#### keywords

`/search/oa/search?q=bacon`

This is a phrase based search on the body, target, bodyURI and targetURI fields of indexed annotation items. The text field of text image items are also searchable here.

`/search/oa/search?q=bacon sandwich`

This will search for the phrase 'bacon sandwich' as written, and will not pull back items with just bacon or sandwich or 'sandwich bacon'.  

#### motivation

Singular or multiple (space separated) motivations can be queried, and we can also search for a non-motivation. If searching for a non-motivation, only one is allowed.

`/search/oa/search?motivation=painting`

`/search/oa/search?motivation=painting tagging`

`/search/oa/search?motivation=non-painting`

When we indexed annotations for motivation we pulled data from any top level **motivation** or resource **purpose** fields. We index the w3c form of an annotation, and this may have motivations that are oa specific. These are usually prefixed by  oa:. We remove this when we index an item so e.g. oa:painting, which has no w3c equivalent is indexed with painting.  

#### user

 singular or multiple (space separated) users can be queried

`/search/oa/search?user=sarah`

`/search/oa/search?user=sarah dave`

When we indexed users we pulled data from any top level **creator** field.

#### date

 Singular or multiple (space separated) dates can be queried

`/search/oa/search?date=1985-05-01T00:00:00+00:00/1986-05-01T00:00:00+00:00`

`/search/oa/search?date=1985-05-01T00:00:00+00:00/1986-05-01T00:00:00+00:00 1995-05-01T00:00:00+00:00/1996-05-01T00:00:00+00:00`

When we indexed dates we pulled data from any top level **created** field. Dates must be in the  ISO8601 format
YYYY-MM-DDThh:mm:ssZ/YYYY-MM-DDThh:mm:ssZ.

We can search on just keywords, just motivation, just user or just date, or a combination of any.  e.g.

`search/oa/search?q=bacon&motivation=painting&user=sarah&date=date=1985-05-01T00:00:00+00:00/1986-05-01T00:00:00+00:00`

Annotations can have motivation, user, date, but Text images by definition are only motivation=painting.

#### page

When we get a response for a search, we may have hundreds or thousands of annotations listed in the response, these are paged when the number of annotations in our list reaches > 100. Results with <= 100
show as non-paged annotation lists. Worth noting that paging will only handle 10,000 items, so if we have 13,678 items returned in our search results, we will only be able to go to page 99. This setting can be altering in the elasticsearch.yml by upping the index.max_result_window from 10000.



#### width/height

For text searches, we may need to scale our results. We can pass the width and height parameters to the coordinate service so that the resulting coordinates are scaled. Should no width/height parameters be passed then the coordinate service should deal with scaling.

When searching annotations and text together, a motivation of non-painting will limit the results to annotations only. Search parameters for text don't have motivation, user or date. Text only requests will only have query, page, width and height parameters. **All query strings must be URL encoded**.





## Search Response:

Regardless of what search is implemented, we return an [annotation list](http://iiif.io/api/presentation/2.1/#annotation-list)  response. Where the number of results are > 100, we allow the user to [page results](http://iiif.io/api/search/1.0/#paging-results).  When searching text we cannot know the exact number of results that will be obtained for each page, as they may be multiple hits for a word/phrase on one text item. In this instance we may not keep each page to 100 items, but allow >100 items per page, the startIndex and total will change to reflect this.

### Annotation-only Search Request:

`http://localhost:8080/search/oa/search/?q=84.jpg&page=1`

### Annotation-only Search Response:

    {
      "@context": "http://iiif.io/api/presentation/2/context.json",
      "@id": "http://localhost:8080/search/oa/search/?q=84.jpg&page=1",
      "@type": "sc:AnnotationList",
      "within": {
        "@type": "sc:Layer",
        "total": "2738",
        "first": "http://localhost:8080/search/oa/search/?q=84.jpg&page=1",
        "last": "http://localhost:8080/search/oa/search/?q=84.jpg&page=28"
      },
      "next": "http://localhost:8080/search/oa/search/?q=84.jpg&page=2",
      "startIndex": "0",
      "resources": [
        {
          "@context": "https://raw.githubusercontent.com/dlcs/elucidate-server/master/elucidate-server/src/main/resources/contexts/oa.jsonld",
          "@id": "http://annotation-local.digtest.co.uk/annotation/oa/156dd8e1-bec5-404d-8a32-e7668d3b750d/989a2cad-bedc-441c-b2c3-98aa638025f8",
          "@type": "oa:Annotation",
          "hasBody": "https://randomuser.me/api/portraits/women/74.jpg",
          "hasTarget": "https://randomuser.me/api/portraits/men/84.jpg"
        },
        {
          "@context": "https://raw.githubusercontent.com/dlcs/elucidate-server/master/elucidate-server/src/main/resources/contexts/oa.jsonld",
          "@id": "http://annotation-local.digtest.co.uk/annotation/oa/156dd8e1-bec5-404d-8a32-e7668d3b750d/dbf34d3a-9fc9-48a0-becb-8eb7af86c76a",
          "@type": "oa:Annotation",
          "hasBody": "https://randomuser.me/api/portraits/men/13.jpg",
          "hasTarget": "https://randomuser.me/api/portraits/women/84.jpg"
        },.....

### Text-only Search Request:

Text search results create annotations on the fly. We have previously indexed text items, which are basically a canvas URI, an image URI and the text itself.  These are fed by a Starsky service. When we search for a phrase in elasticsearch, we find hits, and then pass the offset positions of the hits in the text to a coordinate service.

```
{
  "@context": [
    "http://iiif.io/api/presentation/2/context.json",
    "http://iiif.io/api/search/1/context.json"
  ],
  "@id": "http://localhost:8080/search/search/oa?q=fifty",
  "@type": "sc:AnnotationList",
  "resources": [
    {
      "@id": "http://localhost:8080/search/search/oa/searchResultVIAHTTuH654,278,48,9",
      "@type": "oa:Annotation",
      "motivation": "sc:painting",
      "resource": {
        "@type": "cnt:ContentAsText",
        "chars": "fifty"
      },
      "on": "https://presley.dlcs-ida.org/iiif/idatest01/_roll_M-1011_145_cvs-31-33/canvas/c31#xywh=654,278,48,9"
    }
  ],
  "hits": [
    {
      "@type": "search:Hit",
      "annotations": [
        "http://localhost:8080/search/search/oa/searchResultVIAHTTuH654,278,48,9"
      ],
      "match": "fifty",
      "before": "enrolled during the last year the agency consists of about ",
      "after": " frame and concrete buildings this agenoy and school is"
    }
  ]
}
```



This search result illustrates what happens when we have 2 annotations for one search hit. This means that  we have gone over a line in our text document. The '**concrete**' is at the end of a line, and '**buildings**' at the start of another.

    {
      "@context": [
        "http://iiif.io/api/presentation/2/context.json",
        "http://iiif.io/api/search/1/context.json"
      ],
      "@id": "http://localhost:8080/search/search/oa?q=concrete%20buildings",
      "@type": "sc:AnnotationList",
      "resources": [
        {
          "@id": "http://localhost:8080/search/search/oa/searchResultDNWkMOSI821,278,75,9",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "concrete"
          },
          "on": "https://presley.dlcs-ida.org/iiif/idatest01/_roll_M-1011_145_cvs-31-33/canvas/c31#xywh=821,278,75,9"
        },
        {
          "@id": "http://localhost:8080/search/search/oa/searchResultskyCDKmn122,290,88,9",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "buildings"
          },
          "on": "https://presley.dlcs-ida.org/iiif/idatest01/_roll_M-1011_145_cvs-31-33/canvas/c31#xywh=122,290,88,9"
        }
      ],
      "hits": [
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/search/oa/searchResultDNWkMOSI821,278,75,9",
            "http://localhost:8080/search/search/oa/searchResultskyCDKmn122,290,88,9"
          ],
          "match": "concrete buildings",
          "before": "last year the agency consists of about fifty frame and ",
          "after": " this agenoy and school is situated upon the western"
        }
      ]
    }


At present we do not allow searching that spans image boundaries, as we do not index images with this in mind. In our hit we show 10 words **before** and the 10 words **after** the **match**. We also only show lowercase words for the hit.


### Text-only Search Request:

`http://localhost:8080/search/oa/text/search?q=fifty%20frame`

### Text-only Search Response:

    {
      "@context": [
        "http://iiif.io/api/presentation/2/context.json",
        "http://iiif.io/api/search/1/context.json"
      ],
      "@id": "http://localhost:8080/search/search/oa?q=fifty%20frame",
      "@type": "sc:AnnotationList",
      "resources": [
        {
          "@id": "http://localhost:8080/search/search/oa/searchResulthNbQmoDS654,278,109,10",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "fifty frame"
          },
          "on": "https://presley.dlcs-ida.org/iiif/idatest01/_roll_M-1011_145_cvs-31-33/canvas/c31#xywh=654,278,109,10"
        }
      ],
      "hits": [
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/search/oa/searchResulthNbQmoDS654,278,109,10"
          ],
          "match": "fifty frame",
          "before": "enrolled during the last year the agency consists of about ",
          "after": " and concrete buildings this agenoy and school is situated"
        }
      ]
    }


## Autocomplete Request:

### Intro:

Autocomplete makes use of Elasticsearch [Completion](https://www.elastic.co/guide/en/elasticsearch/reference/5.4/search-suggesters-completion.html) and [Context](https://www.elastic.co/guide/en/elasticsearch/reference/5.4/suggester-context.html) Suggesters.  These are implemented when indexing. We take all the values (parsed by spaces) of the data in an annotation an add this to the documents suggest input field as lowercase String array. For plaintext we add the space separated terms in the plaintext field as input. We use the [Context](https://www.elastic.co/guide/en/elasticsearch/reference/5.4/suggester-context.html) Suggester to hold information on the manifest(s).

### Parameters:

The following query parameters inline with the http://iiif.io/api/search/1.0/#query-parameters-1 have been implemented:

#### keywords

`/search/oa/autocomplete?q=bacon`

The keyword parameter is mandatory for an autocomplete, but motivation, user and date in addition to min have not been implemented.





##Autocomplete Response:



The responses are in line with http://iiif.io/api/search/1.0/#response, however we have omitted the count property. We have not implemented any labels for autocomplete. We limit the number of results returned to 1000.  The only difference between an oa and w3c response is the terms url will have the path for an oa search or a w3c search. We always URL encode the terms:url field.

### Autocomplete request:

`http://localhost:8080/search/w3c/text/autocomplete?q=therapeuti`



### Autocomplete response:

    {
      "@context": "http://iiif.io/api/search/1/context.json",
      "@id": "http://localhost:8080/search/w3c/text/autocomplete?q=therapeuti",
      "@type": "search:TermList",
      "terms": [
          {
            "match": "therapeutics",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutics"
          },
          {
            "match": "therapeutics,",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutics%2C"
          },
          {
            "match": "therapeutics:",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutics%3A"
          },
          {
            "match": "therapeutique,",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutique%2C"
          },
          {
            "match": "therapeutische",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutische"
          },
          {
            "match": "therapeutische,",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutische%2C"
          },
          {
            "match": "therapeutischen",
            "url": "http://localhost:8080/search/w3c/text/search?q=therapeutischen"
          }
        ]
      }
    }



### Topic Autocomplete Requests:

If we were doing the autocomplete for e.g. a specific topic then we would do something like:

Place is an umbrella term for GPE (Geo Political Entity), LOC (Location), FAC (Facilities) etc. Topics can be real or virtual so we need to query for six uri's:

```
https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe/sa

https://omeka.dlcsida.org/s/ida/page/topics/gpe/sa

https://omeka.dlcsida.org/s/ida/page/topics/virtual:loc/sa

https://omeka.dlcsida.org/s/ida/page/topics/loc/sa

https://omeka.dlcsida.org/s/ida/page/topics/virtual:fac/sa

https://omeka.dlcsida.org/s/ida/page/topics/fac/sa

```



We need to URL- encode each uri to autocomplete on e.g.

```
http://mathmos.dlcs-ida.org/search/oa/autocomplete?q=https%3A%2F%2Fomeka.dlcsida.org%2Fs%2Fida%2Fpage%2Ftopics%2Fvirtual%3Agpe%2Fsa
```

and so on for each topic uri..



This will pull back six sets of results, some of which may be empty if say there are no https://omeka.dlcsida.org/s/ida/page/topics/gpe/sa topics.  These will be of the form:



### Topic Autocomplete Responses:

```
{
    "@context": "http://iiif.io/api/search/1/context.json",
    "@id": "http://mathmos.dlcs-ida.org/search/oa/autocomplete?q=https%3A%2F%2Fomeka.dlcsida.org%2Fs%2Fida%2Fpage%2Ftopics%2Fvirtual%3Agpe%2Fsa",
    "@type": "search:TermList",
    "terms": [
        {
            "match": "https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe/samir",
            "url": "http://mathmos.dlcs-ida.org/search/oa/search?q=https%3A%2F%2Fomeka.dlcsida.org%2Fs%2Fida%2Fpage%2Ftopics%2Fvirtual%3Agpe%2Fsamir"
        },
        {
            "match": "https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe/santafe",
            "url": "http://mathmos.dlcs-ida.org/search/oa/search?q=https%3A%2F%2Fomeka.dlcsida.org%2Fs%2Fida%2Fpage%2Ftopics%2Fvirtual%3Agpe%2Fsantafe"
        },
        {
            "match": "https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe/saslar",
            "url": "http://mathmos.dlcs-ida.org/search/oa/search?q=https%3A%2F%2Fomeka.dlcsida.org%2Fs%2Fida%2Fpage%2Ftopics%2Fvirtual%3Agpe%2Fsaslar"
        },
        .....
```

or for an empty response:

```
{
    "@context": "http://iiif.io/api/search/1/context.json",
    "@id": "http://mathmos.dlcs-ida.org/search/oa/autocomplete?q=https://BAD",
    "@type": "search:TermList",
    "terms": []
}
```



Iteration through each response and merging results of the match field minus the autocomplete strings e.g.

```
https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe/samir
```

minus

```
https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe
```

will give you the terms you are looking for e.g.

```
samir
```



We could narrow down the number of requests we need to make by doing an autocomplete as we drilldown to searching for a place topic, as we will know if there is any data for e.g.

```
https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe

https://omeka.dlcsida.org/s/ida/page/topics/gpe

https://omeka.dlcsida.org/s/ida/page/topics/virtual:loc

https://omeka.dlcsida.org/s/ida/page/topics/loc

https://omeka.dlcsida.org/s/ida/page/topics/virtual:fac

https://omeka.dlcsida.org/s/ida/page/topics/fac

```

And if we only get  a response with data for e.g.

```
https://omeka.dlcsida.org/s/ida/page/topics/virtual:gpe
```

Then only make requests for this data. Potential to cache this higher level topic information so that you can periodically query for update how many actual queries you need to make for a place.





# Caching

We have used ehcache to cache the search results from mathmos. This is done on the queryString in annotation only searches. Caching of text and mixed searches is done slightly differently, as we have a potential shift in the total number of results as we page through.

# Within (coming soon)

Within is where we want to limit the results returned to a particular collection. We will Base64 encode the within collection url and use this to call another service that allows us to filter our set of results to a subset only concerned with that collection.



The entry points for within will be:

### Search



#### Annotations only:

`/search/{withinId}/oa/search`

`/search/{withinId}/w3c/search`



#### Text only:

`/search/{withinId}/oa/text/search`

`/search/{withinId}/w3c/text/search`



#### Annotations and text:

`/search/{withinId}/search/oa`

`/search/{withinId}/search/w3c`



### Autocomplete



#### Annotations only:

`/search/{withinId}/oa/autocomplete`

`/search/{withinId}/w3c/autocomplete`



#### Text only:

`/search/{withinId}/oa/text/autocomplete`

`/search/{withinId}/w3c/text/autocomplete`



#### Annotations and text:

`/search/{withinId}/autocomplete/oa`

`/search/{withinId}/autocomplete/w3c`
