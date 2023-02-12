# ElevateAI Java SDK

ElevateAI - the most afforable, accurate Speech-to-text (ASR) API. Free to use for hundreds of hours of audio per month!

Steps  - Pre Req: signup for a free account @ https://app.elevateai.com and retrieve your API token
1. Declare an interaction (give a URI if you want ElevateAI to download the interaction via a Public URI)
2. Store Interaction ID
3. Upload a file if no URI specified during declare using the Interaction ID
4. Check status every 30 seconds using Interaction ID until status is 'processed' or an error status https://docs.elevateai.com/tutorials/check-the-processing-status
5. Retrieve results (transcripts, ai results) https://docs.elevateai.com/tutorials/get-phrase-by-phrase-transcript

#Usage:

```java
import elevateAi.cient.Client;

        ... ... 
        var cli = Client.newInstance(baseUrl, apiToken);

        // 1,2. Declare
        var it = cli.declare("en-us", "default", "highAccuracy", null, null, false);

        // 3. Upload media file
        var uploadOk = cli.upload(it, "d:/dev/elevateai-cli/sample-media/media.wav");

        // 4. Wait for processing completion
        while (true){
            var s = cli.status(it);
            if("processed".equals(s))
                break;
            Thread.sleep(60000);
        }
        var tx = cli.transcripts(it, true);
        var ai = cli.aiResults(it);
        System.out.println(String.format("Interaction [%s]: \nTranscripts: %s, \nAiResults: %s", it, tx, ai));

```
