package me.ayydxn.niagara.demo.core;

import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.sdl.SDLEvents.*;
import static org.lwjgl.sdl.SDLVideo.*;

public final class Window
{
    private final String title;
    private final int width;
    private final int height;

    private OnWindowCloseCallback onWindowCloseCallback;

    private long handle;
    private long glContextHandle;

    public Window(String title, int width, int height)
    {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public void create()
    {
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 6);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);
        SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1);
        SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, 24);

        this.handle = SDL_CreateWindow(this.title, this.width, this.height, SDL_WINDOW_OPENGL | SDL_WINDOW_RESIZABLE);
        if (this.handle == MemoryUtil.NULL)
            throw new IllegalStateException("Failed to create demo app window!");

        this.glContextHandle = SDL_GL_CreateContext(this.handle);

        SDL_GL_MakeCurrent(this.handle, this.glContextHandle);
        SDL_GL_SetSwapInterval(1);

        GL.createCapabilities();
    }

    public void update()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            SDL_Event event = SDL_Event.calloc(memoryStack);
            while (SDL_PollEvent(event))
                this.handleEvent(event);
        }
    }

    public void swapBuffers()
    {
        glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        SDL_GL_SwapWindow(this.handle);
    }

    public void destroy()
    {
        if (this.handle != MemoryUtil.NULL && this.glContextHandle != MemoryUtil.NULL)
        {
            SDL_GL_DestroyContext(this.glContextHandle);
            SDL_DestroyWindow(this.handle);

            this.glContextHandle = MemoryUtil.NULL;
            this.handle = MemoryUtil.NULL;
        }
    }

    public void setOnWindowCloseCallback(OnWindowCloseCallback callback)
    {
        this.onWindowCloseCallback = callback;
    }

    private void handleEvent(SDL_Event event)
    {
        switch (event.type())
        {
            case SDL_EVENT_WINDOW_CLOSE_REQUESTED -> this.onWindowCloseCallback.onWindowClose();

            case SDL_EVENT_WINDOW_RESIZED ->
            {
                int newWindowWidth = event.window().data1();
                int newWindowHeight = event.window().data2();

                if (newWindowWidth > 0 && newWindowHeight > 0)
                    glViewport(0, 0, newWindowWidth, newWindowHeight);
            }
        }
    }

    @FunctionalInterface
    public interface OnWindowCloseCallback
    {
        void onWindowClose();
    }
}
