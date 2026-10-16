<p align="center"><img src="src/main/resources/assets/garipseylermc/icon.png" width="160" alt="Garip Şeyler MC"></p>

# Garip Şeyler MC

Stranger Things temalı bir Minecraft Java modu: Nether kırmızı **Ters Dünya**'ya, End **En Derin Dünya**'ya dönüşür; Demogorgon, Demodog, Demobat, Vecna ve Mind Flayer gelir.

> Durum: **Faz 0 tamamlandı** (altyapı). Özellik fazları için bkz. [ROADMAP.md](ROADMAP.md).

## Gereksinimler
| | Sürüm |
|---|---|
| Minecraft Java | 26.2 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.161.0+26.2 |
| Java | 25 |

## Kurulum (oyuncu)
1. Minecraft 26.2 için Fabric Loader kur.
2. `mods` klasörüne **Fabric API** ve `garipseylermc-<sürüm>.jar` dosyasını koy.
3. Oyunu Fabric profiliyle başlat.

## Geliştirme
```bat
gradlew build        :: jar -> build/libs/
gradlew runClient    :: geliştirme istemcisi
gradlew runServer    :: geliştirme sunucusu (run/ klasörü)
run.bat              :: derle + MehburMC "Garip Şeyler Test" profiline kopyala + launcher'ı aç
```

## Proje yapısı
```
src/main/java/dev/mehbur/garipseyler/      ortak (sunucu + istemci)
├── config/     ModConfig, ConfigManager
├── command/    /garipseyler komutları
├── dimension/  Ters Dünya / En Derin Dünya mantığı
├── entity/     Vecna, Mind Flayer
├── block/      Ters Dünya Taşı, sarmaşık
├── item/       Telekinezi Eldiveni
├── effect/     Vecna Laneti, efekt yardımcıları
└── network/    sunucu → istemci paketleri
src/client/java/dev/mehbur/garipseyler/client/   sadece istemci
├── render/  sky/  hud/  input/
```

## Ayarlar
İlk açılışta `config/garipseylermc.json` oluşur. Değiştirdikten sonra oyunu yeniden başlat ya da `/garipseyler reload` yaz (OP gerekir). Hatalı JSON yazılırsa dosya `.broken` olarak yedeklenir ve varsayılanlar kullanılır.

| Bölüm | Anahtar | Varsayılan | Açıklama |
|---|---|---|---|
| `nether` | `redLightningEnabled` | `true` | Kırmızı şimşek açık/kapalı |
| | `redLightningIntervalSeconds` | `7` | Şimşek aralığı (sn) |
| | `redLightningSound` | `true` | Şimşek sesi |
| `vine` | `drainIntervalMinutes` | `10` | Can düşürme aralığı (dk) |
| | `drainTargetHealth` | `1.0` | Düşürülen can (HP, 2 = 1 kalp) |
| | `effectRadius` | `32` | Etki yarıçapı (blok) |
| | `warningEnabled` / `warningSeconds` | `true` / `30` | Önceden uyarı |
| `vecna` | `maxHealth` | `2000` | Can |
| | `spawnDelayMinutes` | `15` | Nether'da geçen süre sonrası tek seferlik spawn |
| | `gloveDropChance` | `0.001` | Eldiven düşme şansı (%0.1) |
| `mindFlayer` | `maxHealth` | `1500` | Can |
| | `regenDelaySeconds` | `10` | Hasarsız geçmesi gereken süre |
| | `fullRegenSeconds` | `60` | 0 → tam can süresi |
| `glove` | `range` / `cooldownSeconds` / `throwStrength` | `20` / `5` / `2.0` | Telekinezi ayarları |

> Not: Ayarların çoğu ilgili faz tamamlandıkça etkin hale gelir.

## Özellikler
Ayrıntılı liste ve durum: [ROADMAP.md](ROADMAP.md).
