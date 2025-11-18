# StarkJarvis App Icon Guidelines

## Arc Reactor Icon Design

"Make it look like it could power a suit, not like a smartphone app." - Tony Stark

### Design Concept

The app icon should be an **Arc Reactor** design that represents:
- Clean energy (blue glow)
- Advanced technology
- Stark Industries branding
- Instantly recognizable Iron Man aesthetic

### Design Specifications

#### Colors
- **Primary**: Arc Reactor Blue (#00D4FF)
- **Secondary**: Stark Gold (#FFB300)
- **Background**: Pure Black (#000000) or very dark grey
- **Glow**: White (#FFFFFF) for core center

#### Icon Sizes Required

Create the following sizes for Android adaptive icons:

**Launcher Icons (Adaptive)**
- `res/mipmap-mdpi/ic_launcher.png` - 48x48 px
- `res/mipmap-hdpi/ic_launcher.png` - 72x72 px
- `res/mipmap-xhdpi/ic_launcher.png` - 96x96 px
- `res/mipmap-xxhdpi/ic_launcher.png` - 144x144 px
- `res/mipmap-xxxhdpi/ic_launcher.png` - 192x192 px

**Launcher Icons (Round)**
- Same sizes as above with `ic_launcher_round.png` suffix

**Adaptive Icon Layers** (Android 8.0+)
- `res/mipmap-anydpi-v26/ic_launcher.xml`
- Foreground layer: 108x108 dp (Arc reactor design)
- Background layer: Solid black or gradient

**Play Store**
- 512x512 px (high-res icon for Google Play)

#### Design Elements

**Core Structure:**
1. **Center Core** - Small white/blue circle (represents energy source)
2. **Inner Ring** - Pulsing arc reactor core with triangular energy patterns
3. **Middle Ring** - Concentric circles or energy bands
4. **Outer Glow** - Radial gradient from bright blue to transparent
5. **Energy Segments** - 8-12 radial lines or triangular segments

**Visual Style:**
- Minimalist and modern
- Strong contrast (glows on dark background)
- Recognizable at small sizes (48px)
- Avoid too much detail (keep it clean)

### Design Tools

**Recommended:**
1. **Figma** (figma.com) - Free, web-based
2. **Adobe Illustrator** - Professional vector design
3. **Inkscape** - Free, open-source alternative
4. **Android Asset Studio** (romannurik.github.io/AndroidAssetStudio) - Generate all sizes

**AI Generation:**
- DALL-E, Midjourney, or Stable Diffusion
- Prompt: "Arc reactor icon design, glowing blue energy core, circular tech pattern, minimalist, black background, Iron Man style"

### Quick Start - Using Android Asset Studio

1. Go to: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
2. Upload your arc reactor design (512x512 recommended)
3. Adjust shape, background color (black), padding
4. Download zip with all required sizes
5. Extract to `app/src/main/res/`

### Reference Images

**Search online for:**
- "Arc reactor vector"
- "Iron Man arc reactor icon"
- "Circular tech logo blue glow"
- "Stark Industries logo"

**Key Visual References:**
- Movie: Iron Man (2008) - Arc Reactor chest piece
- Movie: Iron Man 2 (2010) - New element arc reactor (triangle pattern)
- Movie: Avengers (2012) - Stark Tower arc reactor

### Implementation

Once you have the icon files:

1. **Replace default icons:**
   ```bash
   # Delete existing launcher icons
   rm -rf app/src/main/res/mipmap-*/ic_launcher*

   # Add your new arc reactor icons to each mipmap folder
   ```

2. **Update AndroidManifest.xml:**
   ```xml
   <application
       android:icon="@mipmap/ic_launcher"
       android:roundIcon="@mipmap/ic_launcher_round"
       ...
   ```

3. **Create adaptive icon XML** (`res/mipmap-anydpi-v26/ic_launcher.xml`):
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
       <background android:drawable="@color/stark_black"/>
       <foreground android:drawable="@drawable/ic_launcher_foreground"/>
   </adaptive-icon>
   ```

### DIY Simple Version

If you want a quick placeholder:

1. Use the existing Arc Reactor animation from `JarvisScreen.kt`
2. Take a screenshot of the animation at peak brightness
3. Crop to square, resize to 512x512
4. Upload to Android Asset Studio
5. Generate all required sizes

### Stark-Approved Checklist

- [ ] Arc reactor design clearly visible at 48x48
- [ ] Blue glow (#00D4FF) prominent
- [ ] Black or very dark background
- [ ] Works in both circular and square shapes
- [ ] All required sizes generated
- [ ] Tested on physical device (icons look crisp)
- [ ] Play Store icon (512x512) ready

## Example Code for Programmatic Icon (Advanced)

If you want to generate the icon programmatically using Jetpack Compose Canvas:

```kotlin
// Use the ArcReactorAnimation composable from SplashActivity
// Render to bitmap at 512x512
// Save as PNG
// Import to Android Asset Studio
```

---

**"Sir, this icon represents the heart of Stark Industries. Make it worthy."** - J.A.R.V.I.S.
