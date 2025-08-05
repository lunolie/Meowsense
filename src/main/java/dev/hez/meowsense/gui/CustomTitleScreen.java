package dev.hez.meowsense.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class CustomTitleScreen extends TitleScreen {
    private static final Identifier CUSTOM_LOGO = Identifier.of("meowsense", "cha/watermarklogo/logo.png");
    private int shaderProgram = -1;
    private int timeUniform = -1;
    private int resolutionUniform = -1;
    private boolean shaderLoaded = false;
    private float time = 0.0f;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        time += delta * 0.05f;

        // Render our custom background
        if (shaderLoaded && shaderProgram != -1) {
            renderShaderBackground(context);
        } else {
            renderFallbackBackground(context);
        }

        // Render watermark and title
        renderWatermarkAndTitle(context);

        // Render default buttons and elements
        super.render(context, mouseX, mouseY, delta);

        // Handle tooltips
        for (var element : this.children()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                element.setFocused(true);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override to prevent any default background rendering
    }

    @Override
    protected void init() {
        super.init();
        loadCustomShader();
    }

    private void loadCustomShader() {
        try {
            System.out.println("Compiling embedded shader...");
            String vertSource = getDefaultVertexShader();
            String fragSource = getEmbeddedFragmentShader();

            shaderProgram = createShaderProgram(vertSource, fragSource);

            if (shaderProgram != -1) {
                timeUniform = GL20.glGetUniformLocation(shaderProgram, "time");
                resolutionUniform = GL20.glGetUniformLocation(shaderProgram, "resolution");
                shaderLoaded = true;
                System.out.println("Embedded shader loaded successfully!");
            } else {
                System.err.println("Failed to create shader program");
            }
        } catch (Exception e) {
            System.err.println("Failed to load embedded shader: " + e.getMessage());
            e.printStackTrace();
            shaderLoaded = false;
        }
    }

    private String getDefaultVertexShader() {
        return """
            #version 330 core
            
            in vec3 Position;
            
            void main() {
                gl_Position = vec4(Position, 1.0);
            }
            """;
    }

    private String getEmbeddedFragmentShader() {
        return """
            #version 330 core
            
            uniform float time;
            uniform vec2 resolution;
            
            out vec4 FragColor;
            
            float hash( float n ) { 
                return fract(sin(n)*753.5453123); 
            }
            
            float noise( in vec2 x )
            {
                vec2 p = floor(x);
                vec2 f = fract(x);
                f = f*f*(3.0-2.0*f);
                
                float n = p.x + p.y*157.0;
                return mix(
                                mix( hash(n+  0.0), hash(n+  1.0),f.x),
                                mix( hash(n+157.0), hash(n+158.0),f.x),
                        f.y);
            }
            
            float fbm(vec2 p, vec3 a)
            {
                 float v = 0.0;
                 v += noise(p*a.x)*.50;
                 v += noise(p*a.y)*.50;
                 v += noise(p*a.z)*.125;
                 return v;
            }
            
            vec3 drawLines( vec2 uv, vec3 fbmOffset, vec3 color1, vec3 color2 )
            {
                float timeVal = time * 0.1;
                vec3 finalColor = vec3( 0.0 );
                for( int i=0; i < 3; ++i )
                {
                    float indexAsFloat = float(i);
                    float amp = 40.0 + (indexAsFloat*5.0);
                    float period = 2.0 + (indexAsFloat+2.0);
                    float thickness = mix( 0.9, 1.0, noise(uv*10.0) );
                    float t = abs( 0.9 / (sin(uv.x + fbm( uv + timeVal * period, fbmOffset )) * amp) * thickness );
                    
                    finalColor +=  t * color1;
                }
                
                for( int i=0; i < 5; ++i )
                {
                    float indexAsFloat = float(i);
                    float amp = 40.0 + (indexAsFloat*0.0);
                    float period = 9.0 + (indexAsFloat+8.0);
                    float thickness = mix( 0.7, 1.0, noise(uv*10.0) );
                    float t = abs( 0.8 / (sin(uv.x + fbm( uv + timeVal * period, fbmOffset )) * amp) * thickness );
                    
                    finalColor +=  t * color2 * 0.6;
                }
                
                return finalColor;
            }
            
            void main() 
            {
                vec2 uv = ( gl_FragCoord.xy / resolution.xy ) * 2.0 - 1.5;
                uv.x *= resolution.x/resolution.y;
                uv.xy = uv.yx;
                vec3 lineColor1 = vec3( 2.3, 0.5, .5 );
                vec3 lineColor2 = vec3( 0.3, 0.5, 2.5 );
                
                vec3 finalColor = vec3(0.0);
                
                float t = sin( time ) * 0.5 + 0.5;
                float pulse = mix( 0.10, 0.20, t);
                
                finalColor += drawLines( uv, vec3( 1.0, 20.0, 30.0), lineColor1, lineColor2 ) * pulse;
                finalColor += drawLines( uv, vec3( 1.0, 2.0, 4.0), lineColor1, lineColor2 );
                
                FragColor = vec4( finalColor, 1.0 );
            }
            """;
    }

    private int createShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = -1;
        int fragmentShader = -1;

        try {
            // Compile vertex shader
            vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vertexShader, vertexSource);
            GL20.glCompileShader(vertexShader);

            if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == 0) {
                System.err.println("Vertex shader compile error: " + GL20.glGetShaderInfoLog(vertexShader));
                return -1;
            }

            // Compile fragment shader
            fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(fragmentShader, fragmentSource);
            GL20.glCompileShader(fragmentShader);

            if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == 0) {
                System.err.println("Fragment shader compile error: " + GL20.glGetShaderInfoLog(fragmentShader));
                return -1;
            }

            // Create and link program
            int program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertexShader);
            GL20.glAttachShader(program, fragmentShader);
            GL20.glLinkProgram(program);

            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
                System.err.println("Shader program link error: " + GL20.glGetProgramInfoLog(program));
                return -1;
            }

            return program;
        } catch (Exception e) {
            System.err.println("Error creating shader program: " + e.getMessage());
            return -1;
        } finally {
            if (vertexShader != -1) GL20.glDeleteShader(vertexShader);
            if (fragmentShader != -1) GL20.glDeleteShader(fragmentShader);
        }
    }

    private void renderShaderBackground(DrawContext context) {
        if (shaderProgram == -1) return;

        try {
            // Save current state
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            // Use our shader
            GL20.glUseProgram(shaderProgram);

            // Set uniforms
            if (timeUniform != -1) {
                GL20.glUniform1f(timeUniform, time);
            }
            if (resolutionUniform != -1) {
                GL20.glUniform2f(resolutionUniform, (float)width, (float)height);
            }

            // Render fullscreen quad
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

            // Fullscreen quad in normalized device coordinates
            buffer.vertex(-1, -1, 0);
            buffer.vertex(1, -1, 0);
            buffer.vertex(1, 1, 0);
            buffer.vertex(-1, 1, 0);

            BufferRenderer.drawWithGlobalProgram(buffer.end());

        } catch (Exception e) {
            System.err.println("Error rendering shader background: " + e.getMessage());
        } finally {
            // Restore state
            GL20.glUseProgram(0);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private void renderFallbackBackground(DrawContext context) {
        // Simple gradient fallback
        float r = 0.1f + 0.05f * (float) Math.sin(time * 0.5f);
        float g = 0.0f + 0.03f * (float) Math.cos(time * 0.3f);
        float b = 0.2f + 0.1f * (float) Math.sin(time * 0.7f);

        int color1 = ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255) | 0xFF000000;
        int color2 = ((int)(r * 128) << 16) | ((int)(g * 128) << 8) | (int)(b * 128) | 0xFF000000;

        context.fillGradient(0, 0, this.width, this.height, color1, color2);
    }

    private void renderWatermarkAndTitle(DrawContext context) {
        String title = "Meowsense";
        String version = "v1.3";

        // Calculate positions for centered layout
        int logoSize = 48;
        int titleWidth = this.textRenderer.getWidth(title) * 2;
        int totalWidth = logoSize + 15 + titleWidth;
        int startX = (this.width - totalWidth) / 2;
        int logoY = 40;
        int titleY = 50;

        // Render the watermark logo
        try {
            RenderSystem.setShaderTexture(0, CUSTOM_LOGO);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Add subtle pulsing effect
            float pulse = 1.0f + 0.1f * (float) Math.sin(time * 2.0f);
            int pulsedSize = (int)(logoSize * pulse);
            int pulseOffset = (pulsedSize - logoSize) / 2;

            context.drawTexture(CUSTOM_LOGO, startX - pulseOffset, logoY - pulseOffset, 0, 0,
                    pulsedSize, pulsedSize, pulsedSize, pulsedSize);

            RenderSystem.disableBlend();
        } catch (Exception e) {
            // Fallback if logo fails to load
            int fallbackColor = 0xFF6600FF;
            context.fill(startX, logoY, startX + logoSize, logoY + logoSize, fallbackColor);
            context.drawCenteredTextWithShadow(this.textRenderer, "MS",
                    startX + logoSize/2, logoY + logoSize/2 - 4, 0xFFFFFF);
        }

        // Main title next to logo
        int textStartX = startX + logoSize + 15;
        context.getMatrices().push();
        context.getMatrices().scale(2.0f, 2.0f, 1.0f);

        // Draw title with animated color
        float colorShift = (float) Math.sin(time * 1.5f);
        int titleColor = 0xFF00FF | ((int)((0.5f + 0.5f * colorShift) * 255) << 8);

        context.drawTextWithShadow(this.textRenderer, title, textStartX / 2, titleY / 2, titleColor);
        context.getMatrices().pop();

        // Version in top right corner
        int versionX = this.width - this.textRenderer.getWidth(version) - 15;
        context.drawTextWithShadow(this.textRenderer, version, versionX, 15, 0xAAAAAA);

        // Subtitle
        String subtitle = "Premium Client";
        int subtitleX = textStartX;
        int subtitleY = titleY + 25;
        context.drawTextWithShadow(this.textRenderer, subtitle, subtitleX, subtitleY, 0x888888);
    }

    @Override
    public void close() {
        if (shaderProgram != -1) {
            try {
                GL20.glDeleteProgram(shaderProgram);
            } catch (Exception e) {
                System.err.println("Error cleaning up shader: " + e.getMessage());
            }
            shaderProgram = -1;
        }
        shaderLoaded = false;
        super.close();
    }
}