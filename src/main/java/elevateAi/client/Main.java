package elevateAi.client;

public class Main {
    public static void main(String argv[]) throws Exception {
        if(argv.length<2){
            System.out.println("Syntax:");
            System.out.println("\njava -jar <libJar> <elevateAi base url> <api token>");
            System.exit(0);
        }

        var baseUrl = argv[0];
        var apiToken = argv[1];

        var cli = Client.newInstance(baseUrl, apiToken);

        // 1,2. Declare
        var it = cli.declare("en-us", "default", "highAccuracy", null, null, false, "sample.wav", "some ext id");

        // 3. Upload media file
        var uploadOk = cli.upload(it, "d:/sample.wav");

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

        /*
        if(argv.length==2){
            var rsp = cli.declare(null, "d:/dev/elevateai-cli/sample-media/AOL00001.wav");
            System.out.println("Declared:\n" + rsp);
        }
        else{
            var rsp = cli.transcripts(argv[2], true);
            System.out.println("Transcripts:\n" + rsp);
        }
         */
    }
}
