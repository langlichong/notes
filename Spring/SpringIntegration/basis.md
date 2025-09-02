<img width="1033" height="694" alt="image" src="https://github.com/user-attachments/assets/c1d7ae2f-af9a-469c-b2e1-c0458d7e3e33" />
<img width="1034" height="711" alt="image" src="https://github.com/user-attachments/assets/b63a35f4-233c-4404-b81e-717bb022342a" />
<img width="1052" height="650" alt="image" src="https://github.com/user-attachments/assets/607d8038-1a71-40f9-b4cb-73866f8b37c4" />
<img width="1029" height="177" alt="image" src="https://github.com/user-attachments/assets/c09b23a0-bb69-4c99-a424-bc99a236a8cb" />


```java

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import java.io.File;

@Configuration
public class FileProcessingFlow {

    private static final String INPUT_DIR = "/tmp/input";

    @Bean
    public IntegrationFlow processFileFlow() {
        return IntegrationFlow
                // 1. The "from()" starts the flow with an Inbound Channel Adapter.
                // It watches the INPUT_DIR for new files.
                .from(Files.inboundAdapter(new File(INPUT_DIR))
                                .autoCreateDirectory(true),
                        // A poller is needed to check the directory periodically.
                        e -> e.poller(Pollers.fixedDelay(5000)))

                // 2. The message payload is a File. Transform it into a String (the file's content).
                // An implicit channel is created between the file adapter and this transformer.
                .transform(Files.toStringTransformer())

                // 3. Transform the string content to uppercase.
                // Another implicit channel is created.
                .transform(String.class, String::toUpperCase)

                // 4. The "handle()" is a Service Activator endpoint.
                // It takes the final message and passes it to a logging handler.
                .handle(message -> {
                    System.out.println("Received transformed content: " + message.getPayload());
                })
                
                // 5. Build the flow.
                .get();
    }
}

```

