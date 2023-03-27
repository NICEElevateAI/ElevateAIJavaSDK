<p align="center">
<img src="https://raw.githubusercontent.com/NICEElevateAI/NICEElevateAI/main/images/ElevateAI-blue-red-logo.png" />
</p>

<div align="center"><a name="menu"></a>
  <h4>
    <a href="https://www.elevateai.com">
      Website
    </a>
    <span> | </span>
    <a href="https://docs.elevateai.com">
      Documentation
    </a>
    <span> | </span>
    <a href="https://www.elevateai.com/blogs">
      Blog
    </a>
  </h4>
</div>

# ElevateAI Java SDK

[ElevateAI](https://www.elevateai.com) provides an API for Speech-to-text (ASR), behavioral analysis and sentiment analysis of voice interactions.

### Example
1. [Signup](https://app.elevateai.com) and retrieve API token from ElevateAI.
1. Declare an interaction. Provide a URI if you want ElevateAI to download the interaction via a Public URI.
2. Retrieve Interaction ID from JSON response and store.
3. Upload a file.
4. Check status every 30 seconds using Interaction ID until status returns 'processed' or an [error status](https://docs.elevateai.com/tutorials/check-the-processing-status).
5. Retrieve results - [phrase-by-phrase transcript](https://docs.elevateai.com/tutorials/get-phrase-by-phrase-transcript), [punctuated transcript](https://docs.elevateai.com/tutorials/get-punctuated-transcript), and [AI results](https://docs.elevateai.com/tutorials/get-cx-ai).


```java
import elevateAi.client.Client;

        ... ... 
        var cli = Client.newInstance(baseUrl, apiToken);

        // Step 2,3
        var it = cli.declare("en-us", "default", "highAccuracy", null, null, false);

        // Step 4
        var uploadOk = cli.upload(it, "d:/dev/elevateai-cli/sample-media/media.wav");

        // Step 5
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
