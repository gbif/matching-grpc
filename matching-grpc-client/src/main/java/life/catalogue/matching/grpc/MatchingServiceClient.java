package life.catalogue.matching.grpc;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class MatchingServiceClient implements AutoCloseable {

    private final MatchServiceGrpc.MatchServiceBlockingStub  blockingStub;
    private final ManagedChannel channel;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public MatchingServiceClient(String target) {
        channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        blockingStub = MatchServiceGrpc.newBlockingStub(channel);
    }

    /** Say hello to server. */
    public NameUsageMatchRpc match(String name) {

        NameUsageQueryRpc request = NameUsageQueryRpc.newBuilder().setScientificName(name).build();
        NameUsageMatchRpc response;
        try {
            response = blockingStub.match(request);
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            return null;
        }
        return response;
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {

        String target = "localhost:9090";
        String query = "Carcharodon carcharias";
        // Allow passing in the user and target strings as command line arguments
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [name [target]]");
                System.err.println("");
                System.err.println("  name    The name to search with");
                System.err.println("  target  The server to connect to. Defaults to " + target);
                System.exit(1);
            }
            query = args[0];
        }
        if (args.length > 1) {
            target = args[1];
        }

        try (MatchingServiceClient client = new MatchingServiceClient(target)){
            NameUsageMatchRpc response = client.match(query);
            System.out.println("[RPC] Matched to: " + response.getUsage());
        }
    }

    @Override
    public void close() throws Exception {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}