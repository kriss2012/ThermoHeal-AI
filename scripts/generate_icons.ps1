Add-Type -AssemblyName System.Drawing

$srcPath = "c:\Users\IMRD\Documents\GitHub\ThermoHeal-AI\icon.png"
$resDir = "c:\Users\IMRD\Documents\GitHub\ThermoHeal-AI\app\src\main\res"

$srcImage = [System.Drawing.Image]::FromFile($srcPath)

function Resize-Image {
    param(
        [System.Drawing.Image]$Image,
        [int]$Width,
        [int]$Height
    )
    $destRect = New-Object System.Drawing.Rectangle(0, 0, $Width, $Height)
    $destImage = New-Object System.Drawing.Bitmap($Width, $Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $destImage.SetResolution($Image.HorizontalResolution, $Image.VerticalResolution)

    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $graphics.DrawImage($Image, $destRect, 0, 0, $Image.Width, $Image.Height, [System.Drawing.GraphicsUnit]::Pixel)
    $graphics.Dispose()

    return $destImage
}

function Create-Circular-Image {
    param(
        [System.Drawing.Image]$Image,
        [int]$Size
    )
    $destImage = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddEllipse(0, 0, $Size, $Size)
    $graphics.SetClip($path)

    $destRect = New-Object System.Drawing.Rectangle(0, 0, $Size, $Size)
    $graphics.DrawImage($Image, $destRect, 0, 0, $Image.Width, $Image.Height, [System.Drawing.GraphicsUnit]::Pixel)

    $graphics.Dispose()
    $path.Dispose()

    return $destImage
}

function Create-Adaptive-Foreground {
    param(
        [System.Drawing.Image]$Image,
        [int]$TotalSize,
        [int]$CenterSize
    )
    $destImage = New-Object System.Drawing.Bitmap($TotalSize, $TotalSize, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $offset = [int](($TotalSize - $CenterSize) / 2)
    $destRect = New-Object System.Drawing.Rectangle($offset, $offset, $CenterSize, $CenterSize)
    $graphics.DrawImage($Image, $destRect, 0, 0, $Image.Width, $Image.Height, [System.Drawing.GraphicsUnit]::Pixel)

    $graphics.Dispose()
    return $destImage
}

# 1. Generate in-app high-res logo
$drawableDir = Join-Path $resDir "drawable"
if (!(Test-Path $drawableDir)) { New-Item -ItemType Directory -Path $drawableDir | Out-Null }
$logo512 = Resize-Image -Image $srcImage -Width 512 -Height 512
$logoPath = Join-Path $drawableDir "ic_thermoheal_logo.png"
$logo512.Save($logoPath, [System.Drawing.Imaging.ImageFormat]::Png)
$logo512.Dispose()
Write-Host "Generated: $logoPath"

# 2. Densities
$densities = @(
    @{ Name = "mipmap-mdpi"; Size = 48; Foreground = 108; Safe = 72 },
    @{ Name = "mipmap-hdpi"; Size = 72; Foreground = 162; Safe = 108 },
    @{ Name = "mipmap-xhdpi"; Size = 96; Foreground = 216; Safe = 144 },
    @{ Name = "mipmap-xxhdpi"; Size = 144; Foreground = 324; Safe = 216 },
    @{ Name = "mipmap-xxxhdpi"; Size = 192; Foreground = 432; Safe = 288 }
)

foreach ($d in $densities) {
    $dir = Join-Path $resDir $d.Name
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

    # Standard launcher
    $resized = Resize-Image -Image $srcImage -Width $d.Size -Height $d.Size
    $launcherPath = Join-Path $dir "ic_launcher.png"
    $resized.Save($launcherPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $resized.Dispose()

    # Round launcher
    $round = Create-Circular-Image -Image $srcImage -Size $d.Size
    $roundPath = Join-Path $dir "ic_launcher_round.png"
    $round.Save($roundPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $round.Dispose()

    # Adaptive Foreground
    $fg = Create-Adaptive-Foreground -Image $srcImage -TotalSize $d.Foreground -CenterSize $d.Safe
    $fgPath = Join-Path $dir "ic_launcher_foreground.png"
    $fg.Save($fgPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $fg.Dispose()

    Write-Host "Generated assets in: $($d.Name)"
}

$srcImage.Dispose()
Write-Host "Icon generation completed successfully!"
