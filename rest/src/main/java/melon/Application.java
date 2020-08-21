package melon;

import io.jooby.Jooby;
import io.jooby.Route;

public class Application extends Jooby {
    {
        // 接收一个handler，产生一个handler
        decorator(next -> ctx -> {
            long start = System.currentTimeMillis();

            Object response = next.apply(ctx);

            long end = System.currentTimeMillis();
            long took = end - start;

            System.out.println("took:" + took + "ms");

            return response;
        });

        before(ctx -> {
           ctx.setResponseHeader("Server", "Jooby");
        });

        after((ctx, result, failure) -> {
            System.out.println(result);
            ctx.setResponseHeader("foo", "bar");
        });

        get("/", ctx -> "decorator");
    }

    public static void main(String[] args) {
        runApp(args, Application::new);
    }
}
