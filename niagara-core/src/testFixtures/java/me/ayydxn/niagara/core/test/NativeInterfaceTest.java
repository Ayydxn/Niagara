package me.ayydxn.niagara.core.test;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.extensions.NativeBackendExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongConsumer;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(NativeBackendExtension.class)
public abstract class NativeInterfaceTest<T>
{
    private final Deque<Runnable> cleanups = new ArrayDeque<>();

    protected T natives;

    protected abstract T selectNatives(NativeBackend nativeBackend);

    protected final NativeBackend backend()
    {
        return NativeBackendManager.getCurrentBackendInstance();
    }

    // Adds a native handle to a queue so that it's destroyed after a test, regardless of if it passed. Returns the same handle as long as it isn't null.
    protected final long track(long handle, LongConsumer destroyer)
    {
        assertNotEquals(0L, handle, "The native call returned a null handle");

        this.cleanups.push(() -> destroyer.accept(handle));

        return handle;
    }

    @BeforeEach
    final void resolveNatives()
    {
        this.natives = this.selectNatives(this.backend());
    }

    @AfterEach
    final void runCleanups()
    {
        RuntimeException failure = null;

        while (!this.cleanups.isEmpty())
        {
            try
            {
                this.cleanups.pop().run();
            }
            catch (RuntimeException exception)
            {
                if (failure == null)
                {
                    failure = exception;
                }
                else
                {
                    failure.addSuppressed(exception);
                }
            }
        }

        if (failure != null)
            throw failure;
    }
}
