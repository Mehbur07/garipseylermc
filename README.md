<p align="center"><img src="src/main/resources/assets/garipseylermc/icon.png" width="160" alt="Garip Şeyler MC"></p>

# Garip Şeyler MC

Stranger Things temalı bir Minecraft Java modu: Nether kırmızı **Ters Dünya**'ya, End **En Derin Dünya**'ya dönüşür; Demogorgon, Demodog, Demobat, Vecna ve Mind Flayer gelir.

> Durum: **Faz 6 tamamlandı** (atmosfer, kırmızı şimşek, mob dönüşümleri, portal sarmaşığı, Ters Dünya Taşı, dikit renkli En Derin Dünya, Vecna boss'u). Özellik fazları için bkz. [ROADMAP.md](ROADMAP.md).

## Gereksinimler
| | Sürüm |
|---|---|
| Minecraft Java | 26.2 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.161.0+26.2 |
| GeckoLib | 5.5.5+ (Fabric 26.2) |
| Java | 25 |

## Kurulum (oyuncu)
1. Minecraft 26.2 için Fabric Loader kur.
2. `mods` klasörüne **Fabric API**, **GeckoLib** (Fabric 26.2, [Modrinth](https://modrinth.com/mod/geckolib)) ve `garipseylermc-<sürüm>.jar` dosyasını koy. Sunucuda da üçü gerekir.
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
| | `upsideDownStoneSpikes` | `true` | Yeni üretilen End kuleleri obsidyen yerine Ters Dünya Taşı'ndan |
| | `dripstoneBlocks` | `true` | End'de End taşı hariç blokları dikit renginde çiz ² ³ |
| | `keepOriginalTexture` | `[]` | End'de kendi dokusunu koruyacak ek bloklar, ör. `["minecraft:torch"]` ² ³ |
| `demobat` | `disableOverworldPhantoms` | `true` | Overworld'de uykusuzluk Phantom/Demobat spawn'ını kapatır |
| | `netherOnly` | `true` | Demobat (Phantom + Ghast) yalnızca Nether'da: Overworld/End'e giren veya orada doğan silinir |
| | `netherSpawnWeight` | `10` | Nether biyomlarında doğal spawn ağırlığı (0 = kapalı) ¹ |
| | `netherMinGroup` / `netherMaxGroup` | `1` / `2` | Grup boyutu ¹ |
| `vine` | `growOnLitPortals` / `growOnGeneratedPortals` | `true` / `true` | Oyuncunun yaktığı / oyunun karşı tarafta ürettiği portallarda büyüsün |
| | `coverage` | `0.65` | Çerçevedeki boş obsidyen yüzlerinin sarmaşıkla kaplanma oranı (0–1) |
| | `growthDurationTicks` | `120` | Alttan üste çıkış animasyonunun süresi (tick, 20 = 1 sn) |
| | `drainIntervalMinutes` | `10` | Can düşürme aralığı (dk) |
| | `drainTargetHealth` | `1.0` | Düşürülen can (HP, 2 = 1 kalp) |
| | `effectRadius` | `32` | Etki yarıçapı (blok) |
| | `warningEnabled` / `warningSeconds` | `true` / `30` | Önceden uyarı |
| `vecna` | `maxHealth` | `2000` | Can |
| | `spawnDelayMinutes` | `15` | Nether'da geçen süre sonrası tek seferlik spawn |
| | `gloveDropChance` | `0.001` | Eldiven düşme şansı (%0.1) |
| | `attackDamage` / `armor` | `12` / `10` | Yakın dövüş hasarı, zırh ¹ |
| | `abilityCooldownSeconds` | `6` | İki yetenek arası (2. fazda %70'i) |
| | `curseDamageIntervalSeconds` / `curseDurationSeconds` | `2` / `20` | Lanet: kaç sn'de 1 hasar (havadayken 2 kat hızlı), süre |
| | `levitationSeconds` / `mindControlSeconds` / `vineBindSeconds` | `4` / `3` / `3` | Havada tutma, zihin kontrolü, sarmaşık bağı süreleri |
| | `psychicBlastDamage` | `8` | Psişik patlama hasarı (zorluğa göre ölçeklenir) |
| | `maxMinions` | `6` | Aynı anda en fazla Demodog + Demobat minyonu |
| | `musicProtectionRadius` | `12` | Çalan müzik kutusunun koruma yarıçapı (blok, 0 = kapalı) |
| `mindFlayer` | `maxHealth` | `1500` | Can |
| | `regenDelaySeconds` | `10` | Hasarsız geçmesi gereken süre |
| | `fullRegenSeconds` | `60` | 0 → tam can süresi |
| `glove` | `range` / `cooldownSeconds` / `throwStrength` | `20` / `5` / `2.0` | Telekinezi ayarları |

¹ Biyom verisine yazıldığı için dünya/sunucu **yeniden başlatılınca** geçerli olur (`/garipseyler reload` yetmez).
² İstemci ayarı: her oyuncunun kendi `config/garipseylermc.json` dosyasından okunur. Flaş ayrıca vanilla **Erişilebilirlik → "Şimşek Flaşlarını Gizle"** seçeneğine uyar.
³ Doku yüklemede uygulanır: değiştirdikten sonra oyunda **F3+T** (ya da yeniden başlat).
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

### Faz 3 — Mob dönüşümleri ✅
Yalnızca **görünüm** değişir; davranış, can, AI, loot ve vuruş kutuları vanilla kalır (eski dünyalarla uyumlu).

| Vanilla | Görünüm | Ayrım |
|---|---|---|
| Enderman | **Demogorgon** | 5 yapraklı ağız; sinirlenince yapraklar açılır, taşıdığı blok görünür |
| Piglin | **Demodog** | normal boy, koyu |
| Piglin Brute | **Yaralı Demodog** | ×1.15, daha koyu, yara izli |
| Zombified Piglin | **Çürümüş Demodog** | ×0.95, soluk yeşil, et lekeli |
| Hoglin | **Alfa Demodog** | ×1.35, kırmızımsı |
| Zoglin | **Çürümüş Alfa Demodog** | ×1.35, yeşilimsi-kırmızı |
| Phantom | **Demobat** | yarasa kanatlı, yaprak ağızlı |
| Ghast | **Dev Demobat** | ×2.75 Demobat, koyu kırmızı; ateş topu hazırlarken ağzı açılır (Happy Ghast değişmez) |

- Bebekler yarım boyda çizilir. Demodog'ların ağız yaprakları saldırırken açılır.
- İsimler, çağırma yumurtaları, ölüm mesajları ve altyazılar yeni adları kullanır (TR + EN). Sesler kendi ses olaylarımıza yönlendirilir.
- **Demobat** ve **Dev Demobat** yalnızca Nether'da bulunur: Overworld'deki uykusuzluk spawn'ı kapalıdır; Overworld'e ya da End'e herhangi bir yolla (portal, komut, eski dünya) gelen Phantom/Ghast bir sonraki tick'te silinir. Bu yüzden "Huzursuz İttifak" başarımı alınamaz. Nether tavanı yüzünden takılmasınlar diye, tavanı olan boyutlarda dönme noktaları mağara tavanının altında tutulur.

### Faz 4 — Ters Dünya sarmaşığı ✅
- Nether portalı **yakıldığında** ve oyun karşı tarafta **portal ürettiğinde** çerçevenin obsidyenlerine **Ters Dünya Sarmaşığı** büyür.
- Çıkış efekti Warden'ın topraktan çıkışı gibidir: `warden.emerge` sesi çalar, sarmaşıklar alttan üste sırayla filizlenir (filiz → genç → tam), her birinde obsidyen parçacıkları saçılır ve Warden kazma sesi gelir.
- **Lanet:** Nether'da, sarmaşıklı bir portalın `effectRadius` (32) blok yakınında geçirilen her `drainIntervalMinutes` (10) dakikada can `drainTargetHealth`'e (1 HP) iner, emicilik kalpleri silinir, kısa bir karanlık gelir. `warningSeconds` (30) önce ekranda uyarı + saat sesi. Asla öldürmez; yaratıcı/izleyici modu etkilenmez.
- Süre oyuncuya kaydedilir (çıkıp girince sıfırlanmaz, ölünce sıfırlanır); yarıçap dışında durur.
- **Karşı hamle:** sarmaşıklar kırılabilir (hiçbir şey düşürmez). Bir portalın tüm sarmaşıkları kırılınca o portalın laneti kalkar.
- Yönetici komutu: `/garipseyler vine <oyuncu>` → sayaç, sarmaşıklı portal sayısı, en yakın mesafe.

### Faz 5 — Ters Dünya Taşı ve En Derin Dünya renkleri ✅
- **Ters Dünya Taşı** (`garipseylermc:upside_down_stone`): dikit tonunda, obsidyenle aynı dayanıklılık — sertlik 50, patlama direnci 1200, elmas ve üstü kazma gerekir, pistonla itilmez, ejderha kıramaz. Yaratıcı modda *Doğal Bloklar* sekmesinde, ağlayan obsidyenin yanında.
- **End kuleleri** artık bu taştan üretilir (yalnızca **yeni üretilen** End; ejderha yeniden çağrılınca kurulan kuleler de). Eski dünyalardaki kuleler obsidyen kalır.
- **En Derin Dünya'da End taşı hariç her blok dikit renginde görünür** (purpur, End taşı tuğlası, obsidyen, koro bitkisi, End çubuğu, oyuncunun koyduğu bloklar…). Sandıklar ve blok-varlıklı bloklar (varil, fırın, demlik, shulker kutusu, yatak, tabela…) ile çalışma masaları, örsler, kazanlar, ateş ve portal kendi görünüşünü korur.
  - Tamamen **görsel / istemci tarafı**: dünya verisi değişmez, Overworld ve Nether vanilla görünür. Kopyalar oyun açılırken her `textures/block` dokusundan üretilir (kaynak paketleri ve diğer modlar dahil), mod içinde Mojang dokusu yoktur.

### Faz 6 — Vecna ✅
- **Doğuş:** her oyuncunun Nether'da geçirdiği süre sayılır (oyuncuya kaydedilir). Biri `spawnDelayMinutes` (15 dk) dolduğunda Vecna 10–18 blok yakınında doğar: saat çalar, karanlık çöker. **Her dünyada yalnızca 1 kez** (bayrak dünya kaydında). Çağırma yumurtası yok, `/summon` reddedilir, Nether'dan çıkamaz (portal kullanmaz), **despawn olmaz** (Barışçıl'da bile).
- **2000 can** (vanilla'nın 1024 can sınırı mod tarafından yükseltilir), 10 zırh, geri tepmeye bağışık, ateşe/düşmeye dayanıklı. Boss bar **mor**; can %50'nin altına inince **kırmızı** olur (2. faz: "Vecna'nın öfkesi uyanıyor!").
- **Yetenekler** (birkaç saniyede bir, rastgele, her birinin kendi bekleme süresi var):
  1. **Lanet** — 20 sn boyunca 2 sn'de 1 hasar; açlıkla gelen doğal can yenilenmesi durur (iksir/altın elma çalışır).
  2. **Havaya kaldırma** — oyuncu 4 blok yükselir, 4 sn havada donar; bu sırada lanet 2 kat hızlı işler.
  3. **Zihin kontrolü** — kamera Vecna'ya çekilir, 3 sn hareket tuşları ters çalışır, mide bulantısı + karanlık.
  4. **Vizyonlar** — havaya kaldırma ve zihin kontrolünden önce 4 sn: 4 kez saat çanı, nabız gibi atan kırmızı kenar karartması, orada olmayan moblardan sahte sesler (yalnızca hedef oyuncu duyar).
  5. **Telekinezi** — yakındaki oyuncuyu fırlatır, uzaktakini kendine çeker.
  6. **Sarmaşık bağı** — 3 sn yerinde sabitler (yürüyemez, zıplayamaz; bakabilir ve vurabilir).
  7. **Psişik patlama** — uzaktan enerji topu: büyü hasarı + kısa karanlık; blok kırmaz, yangın çıkarmaz.
  8. **Minyon çağırma** — 2 Çürümüş Demodog + 1 Demobat (en fazla `maxMinions`); minyonlar Vecna'ya saldırmaz.
  9. **Kısa ışınlanma** — yalnızca 2. fazda: hedefin 5–10 blok yakınına; vurulunca da bazen kaçar.
  10. **Karşı mekanik (müzik)** — elinde **müzik diski** tutan ya da **12 blok** içinde **çalan bir müzik kutusu** olan oyuncu havaya kaldırılamaz ve lanetlenemez (aktif lanet de kalkar): "Müzik seni Vecna'dan koruyor...".
- **Ganimet:** yalnızca **%0.1** şansla **Telekinezi Eldiveni**; başka eşya ve XP düşmez. (Eldivenin gücü Faz 7'de gelecek; şimdilik ödül eşyası.)
- Model ve animasyonlar **GeckoLib** ile (boşta, yürüme, büyü, havaya kaldırma, çağırma, vuruş, ölüm); gözler ve göğüsteki damarlar karanlıkta parlar.
- Yönetici komutları (OP):
  - `/garipseyler vecna status` → doğdu mu / yenildi mi, senin Nether süren
  - `/garipseyler vecna spawn [oyuncu]` → tek seferlik doğuşu hemen yap (zaten doğduysa reddedilir; oyuncu Nether'da olmalı)
  - `/garipseyler vecna reset` → bayrağı sıfırla (test için)
  - `/garipseyler vecna ability <curse|levitate|mind_control|vision|telekinesis|vine_bind|psychic_blast|summon|teleport> [oyuncu]` → en yakın Vecna o yeteneği hemen kullanır

## Placeholder dosyaları (değiştirmek için)
Aynı isimle üzerine yaz, `gradlew build` / `run.bat` ile yeniden derle.

| Dosya | Ne | Not |
|---|---|---|
| `src/main/resources/assets/garipseylermc/textures/entity/demogorgon/demogorgon.png` | Demogorgon dokusu | 64×64 |
| `src/main/resources/assets/garipseylermc/textures/entity/demodog/demodog*.png` | 5 Demodog varyantı (`demodog`, `_brute`, `_rotten`, `_alpha`, `_alpha_rotten`) | 64×64, aynı UV düzeni |
| `src/main/resources/assets/garipseylermc/textures/entity/demobat/demobat.png` | Demobat dokusu | 64×64 |
| `src/main/resources/assets/garipseylermc/textures/entity/demobat/demobat_giant.png` | Dev Demobat (Ghast) dokusu | 64×64, Demobat ile aynı UV düzeni |
| `src/main/resources/assets/garipseylermc/textures/block/upside_down_stone.png` | Ters Dünya Taşı | 16×16 |
| `src/client/java/dev/mehbur/garipseyler/client/render/*/…Model.java` → `createBodyLayer()` | Mob modelleri | Blockbench → *File → Export → Export Java Entity* (Mojang mappings); kutuları `createBodyLayer()` içine taşı, **parça isimlerini koru** (`head`, `body`, `petal_holder_N`…) |
| `src/main/resources/assets/garipseylermc/textures/particle/upside_down_spore_0..2.png` | Ters Dünya spor partikülü | Şeffaf PNG, 8×8 veya 16×16; kare sayısı `particles/upside_down_spore.json`'da |
| `src/main/resources/assets/minecraft/textures/environment/end_sky.png` | En Derin Dünya gökyüzü | Oyun bu dokuyu ~%16 parlaklıkla çarpar → **parlak** çiz, döşenebilir (tileable) olsun |
| `src/main/resources/assets/garipseylermc/textures/block/upside_down_vine_0..2.png` | Sarmaşık büyüme evreleri (filiz, genç, tam) | 16×16, şeffaf arka plan |
| `src/main/resources/assets/garipseylermc/geckolib/models/entity/vecna.geo.json` | Vecna modeli | Blockbench → *GeckoLib Animated Model* (Bedrock geometri); kemik adlarını koru: `root`, `body`, `head`, `left_arm`, `right_arm`, `left_hand`, `right_hand`, `left_leg`, `right_leg`, `tendrils` |
| `src/main/resources/assets/garipseylermc/geckolib/animations/entity/vecna.animation.json` | Vecna animasyonları | Adlar: `misc.idle`, `move.walk`, `attack.cast`, `attack.levitate`, `attack.summon`, `attack.swing`, `misc.die` |
| `src/main/resources/assets/garipseylermc/textures/entity/vecna.png` + `vecna_glowmask.png` | Vecna dokusu + parlayan kısımlar | 128×128; glowmask'ta yalnızca parlayacak pikseller dolu, gerisi şeffaf |
| `src/main/resources/assets/garipseylermc/textures/item/telekinesis_glove.png` | Telekinezi Eldiveni | 16×16 |
| `src/main/resources/assets/garipseylermc/textures/mob_effect/vecna_curse.png` | Lanet efekti simgesi | 18×18 |
| `src/main/resources/assets/garipseylermc/sounds.json` | Ambiyans/müzik/şimşek/mob/sarmaşık (`block.upside_down_vine.warning`, `.drain`) sesleri (`entity.demogorgon.*`, `entity.demodog.*`, `entity.demodog_alpha.*`, `entity.demobat.*`, `entity.demobat_giant.*`, `entity.vecna.*`: ambient, hurt, death, spawn, cast, clock, psychic_blast, shielded) | Şu an vanilla seslerine bağlı. Kendi sesin için `.ogg` dosyasını `assets/garipseylermc/sounds/` altına koy ve `"name"`i `"garipseylermc:<dosya_adı>"` yap (uzantısız) |
