<p align="center"><img src="src/main/resources/assets/garipseylermc/icon.png" width="160" alt="Garip Şeyler MC"></p>

# Garip Şeyler MC

Stranger Things temalı bir Minecraft Java modu: Nether kırmızı **Ters Dünya**'ya, End **En Derin Dünya**'ya dönüşür; Demogorgon, Demodog, Demobat, Vecna ve Mind Flayer gelir.

> Durum: **Faz 2 tamamlandı** (atmosfer + kırmızı şimşek). Özellik fazları için bkz. [ROADMAP.md](ROADMAP.md).

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
| `nether` | `titleOnEnter` | `true` | Girişte "Ters Dünya" başlığı |
| | `fogColor` | `"#3d0a0d"` | Ters Dünya sis rengi ¹ |
| | `ambientLightColor` | `"#3a1414"` | Ortam ışığı tonu ¹ |
| | `fogStartDistance` / `fogEndDistance` | `4.0` / `72.0` | Sis başlangıç/bitiş mesafesi ¹ |
| | `sporeProbability` | `0.03` | Spor partikülü yoğunluğu (0–1) ¹ |
| | `redLightningEnabled` | `true` | Kırmızı şimşek açık/kapalı |
| | `redLightningIntervalSeconds` | `7` | Şimşek aralığı (sn) |
| | `redLightningMinDistance` / `redLightningMaxDistance` | `16` / `48` | Oyuncudan yatay çakma mesafesi (blok) |
| | `redLightningSound` | `true` | Gök gürültüsü sesi ² |
| | `redLightningFlash` / `redLightningFlashIntensity` | `true` / `0.35` | Kırmızı ekran flaşı ve en yüksek opaklığı (0–1) ² |
| `end` | `titleOnEnter` | `true` | Girişte "En Derin Dünya" başlığı |
| | `fogColor` / `ambientLightColor` / `skyLightColor` | `"#14070a"` / `"#3a1f22"` / `"#8a2a3a"` | En Derin Dünya renkleri ¹ |
| | `ashProbability` | `0.004` | Düşen kül partikülü yoğunluğu ¹ |
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

¹ Biyom verisine yazıldığı için dünya/sunucu **yeniden başlatılınca** geçerli olur (`/garipseyler reload` yetmez).
² İstemci ayarı: her oyuncunun kendi `config/garipseylermc.json` dosyasından okunur. Flaş ayrıca vanilla **Erişilebilirlik → "Şimşek Flaşlarını Gizle"** seçeneğine uyar.
Renkler `#RRGGBB` biçiminde; hatalı renk yazılırsa varsayılan kullanılır.

> Not: Ayarların bir kısmı ilgili faz tamamlandıkça etkin hale gelir.

## Özellikler
Ayrıntılı liste ve durum: [ROADMAP.md](ROADMAP.md).

### Faz 1 — Ters Dünya ve En Derin Dünya ✅
- Nether'ın **tüm biyomları** (Crimson, Warped, Soul Sand Valley, Basalt Deltas, Wastes ve modlu Nether biyomları) tek bir kırmızı Ters Dünya gibi görünür: sis, ortam ışığı, spor partikülleri, ambiyans sesi, fısıltılar, müzik. Arazi, yapılar ve mob spawn'ları değişmez.
- End **En Derin Dünya** olur: kan kırmızısı-siyah gökyüzü, kırmızı ışık tonu, düşen kül, alçak uğultu ambiyansı.
- Boyuta girerken başlık kartı ("Ters Dünya" / "En Derin Dünya"), ilgili başarım adları değişir (TR + EN).

### Faz 2 — Kırmızı şimşek ✅
- Ters Dünya'da her **7 sn**'de bir, her oyuncu grubunun yakınında kırmızı şimşek çakar: mağara zemininden tavana uzanan kırmızı yıldırım, kırmızı ekran flaşı, gök gürültüsü.
- **Tamamen görsel:** şimşek yalnızca istemcide oluşturulur; sunucuda varlık yoktur → hasar, yangın, mob dönüşümü (domuz→piglin vb.) **olamaz**.
- Çok oyunculuda yakındaki herkes aynı şimşeği görür.

## Placeholder dosyaları (değiştirmek için)
Aynı isimle üzerine yaz, `gradlew build` / `run.bat` ile yeniden derle.

| Dosya | Ne | Not |
|---|---|---|
| `src/main/resources/assets/garipseylermc/textures/particle/upside_down_spore_0..2.png` | Ters Dünya spor partikülü | Şeffaf PNG, 8×8 veya 16×16; kare sayısı `particles/upside_down_spore.json`'da |
| `src/main/resources/assets/minecraft/textures/environment/end_sky.png` | En Derin Dünya gökyüzü | Oyun bu dokuyu ~%16 parlaklıkla çarpar → **parlak** çiz, döşenebilir (tileable) olsun |
| `src/main/resources/assets/garipseylermc/sounds.json` | Ambiyans/müzik/şimşek sesleri | Şu an vanilla seslerine bağlı. Kendi sesin için `.ogg` dosyasını `assets/garipseylermc/sounds/` altına koy ve `"name"`i `"garipseylermc:<dosya_adı>"` yap (uzantısız) |
