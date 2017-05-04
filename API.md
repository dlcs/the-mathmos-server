# Mathmos API

The Mathmos is an entry point for searching annotations and text images. These are indexed into elasticsearch using separate indexes and mappings. For annotations we will have indexed any annotation that is uploaded into the Elucidate server. For text, we will have indexed any text that has been fed to us by Starsky. Text searching requires further lookups from a coordinate service; given a text phrase and its position in an indexed text item, give us the xywh coordinates for that phrase in the corresponding image, possibly scaled with width and height input parameters). These are then made into annotations on the fly when searched, and in addition to resources, also yield hits.
 
### Entry points:
There are a number of entry points into the Mathmos. These are for search and autocomplete, both for text and annotations alone and for both together, and for **w3c** and **oa**.

These are:

 **Search**
			
*Annotations only:*
> /search/**oa**/search
> /search/**w3c**/search

*Text only:*
> /search/**oa**/text/search
> /search/**w3c**/text/search

*Both text and annotations:*
> /search/search/**oa**
> /search/search/**w3c**


 **Autocomplete**

*Annotations only:*
> /search/**oa**/autocomplete
> /search/**w3c**/autocomplete

*Text only:*
> /search/**oa**/text/autocomplete
> /search/**w3c**/text/autocomplete

*Both text and annotations:*
> /search/autocomplete/**oa**
> /search/autocomplete/**w3c**



## Search Request:

Parameters:

The following parameters (inline with http://iiif.io/api/search/1.0/#query-parameters) are allowed in a mathmos search:

 **keywords**  

>    /search/oa/search?**q=bacon**

This is a phrase based search on the body, target, bodyURI and targetURI fields of indexed annotation items. The text field of text image items are also searchable here.

>    /search/oa/search?**q=bacon sandwich**

This will search for the phrase 'bacon sandwich' as written, and will not pull back items with just bacon or sandwich or 'sandwich bacon'.  

 **motivation** 
Singular or multiple (space separated) motivations can be queried, and we can also search for a non-motivation. If searching for a non-motivation, only one is allowed.

>  /search/oa/search?**motivation=painting**
>  /search/oa/search?**motivation=painting tagging**
>  /search/oa/search?**motivation=non-painting**
 
When we indexed annotations for motivation we pulled data from any top level **motivation** or resource **purpose** fields. We index the w3c form of an annotation, and this may have motivations that are oa specific. These are usually prefixed by  oa:. We remove this when we index an item so e.g. oa:painting, which has no w3c equivalent is indexed with painting.  
 
 **user** 
 singular or multiple (space separated) users can be queried
 >  /search/oa/search?**user=sarah**
 >  /search/oa/search?**user=sarah dave**
 
When we indexed users we pulled data from any top level **creator** field.

 **date**
 Singular or multiple (space separated) dates can be queried
 >  /search/oa/search?**date=1985-05-01T00:00:00+00:00/1986-05-01T00:00:00+00:00**
 >  /search/oa/search?**date=1985-05-01T00:00:00+00:00/1986-05-01T00:00:00+00:00 1995-05-01T00:00:00+00:00/1996-05-01T00:00:00+00:00**
 
When we indexed dates we pulled data from any top level **created** field. Dates must be in the  ISO8601 format 
YYYY-MM-DDThh:mm:ssZ/YYYY-MM-DDThh:mm:ssZ.

We can search on just keywords, just motivation, just user or just date, or a combination of any.  e.g. 

>  /search/oa/search?**q=bacon&motivation=painting&user=sarah&date=date=1985-05-01T00:00:00+00:00/1986-05-01T00:00:00+00:00**

Annotations can have motivation, user, date, but Text images by definition are only motivation=painting. 

**page**
When we get a response for a search, we may have hundreds or thousands of annotations listed in the response, these are paged when the number of annotations in our list reaches > 100. Results with <= 100
show as non-paged annotation lists. Worth noting that paging will only handle 10,000 items, so if we have 13,678 items returned in our search results, we will only be able to go to page 99. This setting can be altering in the elasticsearch.yml by upping the index.max_result_window from 10000.

**width**/**height**
For text searches, we may need to scale our results. We can pass the width and height parameters to the coordinate service so that the resulting coordinates are scaled. Should no width/height parameters be passed then the coordinate service should deal with scaling. 


When searching annotations and text together, a motivation of non-painting will limit the results to annotations only. Search parameters for text don't have motivation, user or date. Text only requests will only have query, page, width and height parameters. **All query strings must be URL encoded** 

## Search Response:

Regardless of what search is implemented, we return an [annotation list](http://iiif.io/api/presentation/2.1/#annotation-list)  response. Where the number of results are > 100, we allow the user to [page results](http://iiif.io/api/search/1.0/#paging-results).  When searching text we cannot know the exact number of results that will be obtained for each page, as they may be multiple hits for a word/phrase on one text item. In this instance we may not keep each page to 100 items, but allow >100 items per page, the startIndex and total will change to reflect this.

Annotation-only Search Request:

> http://localhost:8080/search/oa/search/?q=84.jpg&page=1

Annotation-only Search Response:

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

Text-only Search Request:
Text search results create annotations on the fly. We have previously indexed text items, which are basically an image URI and the text itself.  These are fed by a Starsky service. When we search for a phrase in elasticsearch, we find hits, and then pass the offset positions of the hits in the text to a coordinate service.
 
This search result illustrates what happens when we have 2 annotations for one search hit. This means that  we have gone over a line in our text document. The '**the**' is at the end of a line, and '**school**' at the start of another. 

    {
              "@id": "http://localhost:8080/search/oa/text/search/searchResultORwOiaWl823,366,36,5",
              "@type": "oa:Annotation",
              "motivation": "sc:painting",
              "resource": {
                "@type": "cnt:ContentAsText",
                "chars": "the"
              },
              "on": "https://dlcs.io/iiif-img/wellcome/1/6cfda09d-a43f-4822-882d-5f49addc4aeb#xywh=823,366,36,5"
            },
            {
              "@id": "http://localhost:8080/search/oa/text/search/searchResultAWlLxLJJ293,383,67,5",
              "@type": "oa:Annotation",
              "motivation": "sc:painting",
              "resource": {
                "@type": "cnt:ContentAsText",
                "chars": "school"
              },
              "on": "https://dlcs.io/iiif-img/wellcome/1/6cfda09d-a43f-4822-882d-5f49addc4aeb#xywh=293,383,67,5"
            }
    {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/oa/text/search/searchResultORwOiaWl823,366,36,5",
            "http://localhost:8080/search/oa/text/search/searchResultAWlLxLJJ293,383,67,5"
          ],
          "match": "the school",
          "before": "on the practice of medicine and on clinical medicine in ",
          "after": " of medicine edinburgh edinburgh oliver and boyd tweeddale court"
        }


At present we do not allow searching that spans image boundaries, as we do not index images with this in mind.

> http://localhost:8080/search/oa/text/search?q=the%20school

    {
      "@context": "http://iiif.io/api/presentation/2/context.json",
      "@id": "http://localhost:8080/search/oa/text/search?q=the%20school",
      "@type": "sc:AnnotationList",
      "resources": [
        {
          "@id": "http://localhost:8080/search/oa/text/search/searchResultlysVzHgG384,358,126,9",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "the school"
          },
          "on": "https://dlcs.io/iiif-img/wellcome/1/9af6302a-0654-4e28-9097-1710b7e9b105#xywh=384,358,126,9"
        },
        {
          "@id": "http://localhost:8080/search/oa/text/search/searchResultORwOiaWl823,366,36,5",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "the"
          },
          "on": "https://dlcs.io/iiif-img/wellcome/1/6cfda09d-a43f-4822-882d-5f49addc4aeb#xywh=823,366,36,5"
        },
        {
          "@id": "http://localhost:8080/search/oa/text/search/searchResultAWlLxLJJ293,383,67,5",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "school"
          },
          "on": "https://dlcs.io/iiif-img/wellcome/1/6cfda09d-a43f-4822-882d-5f49addc4aeb#xywh=293,383,67,5"
        },
        {
          "@id": "http://localhost:8080/search/oa/text/search/searchResultUKxQhzqr445,567,108,7",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "the school"
          },
          "on": "https://dlcs.io/iiif-img/wellcome/1/60b4559e-de53-41ef-88d7-c79ea7fe5e91#xywh=445,567,108,7"
        },
        {
          "@id": "http://localhost:8080/search/oa/text/search/searchResultciEDJpJL194,672,140,10",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "the school"
          },
          "on": "https://dlcs.io/iiif-img/wellcome/1/824aec5e-0d31-4f46-bf9b-6a69001db209#xywh=194,672,140,10"
        }
      ],
      "hits": [
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/oa/text/search/searchResultlysVzHgG384,358,126,9"
          ],
          "match": "the school",
          "before": "1 and the eev archdeacon farrar 2 these authors and ",
          "after": " represented by them are by no means inclined to"
        },
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/oa/text/search/searchResultORwOiaWl823,366,36,5",
            "http://localhost:8080/search/oa/text/search/searchResultAWlLxLJJ293,383,67,5"
          ],
          "match": "the school",
          "before": "on the practice of medicine and on clinical medicine in ",
          "after": " of medicine edinburgh edinburgh oliver and boyd tweeddale court"
        },
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/oa/text/search/searchResultUKxQhzqr445,567,108,7"
          ],
          "match": "the school",
          "before": "institution established in england for the education of deaf mutes ",
          "after": " founded by him in edinburgh has been kept up"
        },
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/oa/text/search/searchResultciEDJpJL194,672,140,10"
          ],
          "match": "the school",
          "before": "of being at school again brings back with painful vividness ",
          "after": " feelings and before him who is drowning every event"
        }
      ]
    }
Text-only Search Request:
> http://localhost:8080/search/oa/text/search?q=normal%20school

Text-only Search Response:

    {
      "@context": "http://iiif.io/api/presentation/2/context.json",
      "@id": "http://localhost:8080/search/oa/text/search?q=normal%20school",
      "@type": "sc:AnnotationList",
      "resources": [
        {
          "@id": "http://localhost:8080/search/oa/text/search/searchResultptHZuiXE259,245,188,11",
          "@type": "oa:Annotation",
          "motivation": "sc:painting",
          "resource": {
            "@type": "cnt:ContentAsText",
            "chars": "normal school"
          },
          "on": "https://dlcs.io/iiif-img/wellcome/1/25de8ede-0164-4551-9ce8-3b282fb4d590#xywh=259,245,188,11"
        }
      ],
      "hits": [
        {
          "@type": "search:Hit",
          "annotations": [
            "http://localhost:8080/search/oa/text/search/searchResultptHZuiXE259,245,188,11"
          ],
          "match": "normal school",
          "before": "english by mr h w brown teacher in the state ",
          "after": " worcester mass u.s.a besides treating of the development of"
        }
      ]
    }


## Autocomplete Request:

Parameters:

The following parameters inline with the http://iiif.io/api/search/1.0/#query-parameters-1 have been implemented:

**keywords**  

>    /search/oa/autocomplete?**q=bacon**

The keyword parameter is mandatory for an autocomplete, but motivation, user and date in addition to min have not been implemented.





##Autocomplete Response:



The responses are in line with http://iiif.io/api/search/1.0/#response, however we have omitted the count property. We have not implemented any labels for autocomplete. We limit the number of results returned to 1000. 

**Example of w3c autocomplete request:**

> http://localhost:8080/search/w3c/text/autocomplete?q=therapeuti

**Example of w3c autocomplete response:**

    {
      "@context": "http://www.w3.org/ns/anno.jsonld",
      "@id": "http://localhost:8080/search/w3c/text/autocomplete?q=therapeuti",
      "@type": "http://iiif.io/api/search/1#hasTermList",
      "http://iiif.io/api/search/1#hasTermList": {
        "@list": [
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

 Example of an oa autocomplete request:
 

> http://localhost:8080/search/oa/text/autocomplete?q=therapeuti

 Example of an oa autocomplete response:

    {
      "@context": "http://iiif.io/api/search/1/context.json",
      "@id": "http://localhost:8080/search/oa/text/autocomplete?q=therapeuti",
      "@type": "search:TermList",
      "terms": [
        {
          "match": "therapeutics",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutics"
        },
        {
          "match": "therapeutics,",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutics%2C"
        },
        {
          "match": "therapeutics:",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutics%3A"
        },
        {
          "match": "therapeutique,",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutique%2C"
        },
        {
          "match": "therapeutische",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutische"
        },
        {
          "match": "therapeutische,",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutische%2C"
        },
        {
          "match": "therapeutischen",
          "url": "http://localhost:8080/search/oa/text/search?q=therapeutischen"
        }
      ]
    }
# Caching

We have used ehcache to cache the search results from mathmos. This is done on the queryString in annotation only searches. Caching of text and mixed searches is done slightly differently, as we have a potential shift in the total number of resutls as we page through. 