package dev.mehbur.garipseyler.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.mehbur.garipseyler.block.VineSites;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.effect.ModAttachments;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayer;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayerAttack;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayerFight;
import dev.mehbur.garipseyler.entity.vecna.Vecna;
import dev.mehbur.garipseyler.entity.vecna.VecnaAbility;
import dev.mehbur.garipseyler.entity.vecna.VecnaSpawning;
import dev.mehbur.garipseyler.entity.vecna.VecnaWorldData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * {@code /garipseyler ...} admin commands.
 */
public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("garipseyler")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.literal("reload").executes(context -> {
							ConfigManager.load();
							context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.reload"), true);
							return 1;
						}))
						.then(Commands.literal("vine")
								.then(Commands.argument("player", EntityArgument.player()).executes(ModCommands::vineStatus)))
						.then(vecnaCommand())
						.then(mindFlayerCommand())
		));
	}

	/**
	 * {@code /garipseyler vecna spawn [player]} – one-time spawn near a player (refused once he has spawned
	 * in this world), {@code reset} – allow spawning again (testing), {@code status}, and
	 * {@code ability <name> [player]} – make the nearest Vecna use an ability now.
	 */
	private static LiteralArgumentBuilder<CommandSourceStack> vecnaCommand() {
		RequiredArgumentBuilder<CommandSourceStack, String> ability = Commands.argument("ability", StringArgumentType.word())
				.suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(VecnaAbility.values()).map(VecnaAbility::id), builder))
				.executes(context -> vecnaAbility(context, context.getSource().getPlayerOrException()))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(context -> vecnaAbility(context, EntityArgument.getPlayer(context, "player"))));
		return Commands.literal("vecna")
				.then(Commands.literal("spawn")
						.executes(context -> vecnaSpawn(context, context.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(context -> vecnaSpawn(context, EntityArgument.getPlayer(context, "player")))))
				.then(Commands.literal("reset").executes(ModCommands::vecnaReset))
				.then(Commands.literal("status").executes(ModCommands::vecnaStatus))
				.then(Commands.literal("ability").then(ability));
	}

	private static int vecnaSpawn(CommandContext<CommandSourceStack> context, ServerPlayer player) {
		VecnaWorldData data = VecnaWorldData.get(context.getSource().getServer());
		if (data.spawned()) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.vecna.already_spawned"));
			return 0;
		}
		if (player.level().dimension() != Level.NETHER) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.vecna.not_in_nether"));
			return 0;
		}
		Vecna vecna = VecnaSpawning.spawnNear(player.level(), player);
		if (vecna == null) {
			return 0;
		}
		data.markSpawned();
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.vecna.spawned",
				vecna.blockPosition().getX(), vecna.blockPosition().getY(), vecna.blockPosition().getZ()), true);
		return 1;
	}

	private static int vecnaReset(CommandContext<CommandSourceStack> context) {
		VecnaWorldData.get(context.getSource().getServer()).reset();
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.vecna.reset"), true);
		return 1;
	}

	private static int vecnaStatus(CommandContext<CommandSourceStack> context) {
		VecnaWorldData data = VecnaWorldData.get(context.getSource().getServer());
		ServerPlayer player = context.getSource().getPlayer();
		int seconds = player == null ? 0 : player.getAttachedOrCreate(ModAttachments.NETHER_TICKS) / 20;
		int needed = ConfigManager.get().vecna.spawnDelayMinutes * 60;
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.vecna.status",
				data.spawned(), data.defeated(), seconds, needed), false);
		return data.spawned() ? 1 : 0;
	}

	private static int vecnaAbility(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		String name = StringArgumentType.getString(context, "ability");
		VecnaAbility ability = Arrays.stream(VecnaAbility.values()).filter(a -> a.id().equals(name)).findFirst().orElse(null);
		if (ability == null) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.vecna.unknown_ability", name));
			return 0;
		}
		ServerLevel level = target.level();
		List<Vecna> found = level.getEntitiesOfClass(Vecna.class, target.getBoundingBox().inflate(64.0), Vecna::isAlive);
		Vecna vecna = found.stream().min(Comparator.comparingDouble(v -> v.distanceToSqr(target))).orElse(null);
		if (vecna == null) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.vecna.none_nearby"));
			return 0;
		}
		vecna.setTarget(target);
		vecna.forceAbility(ability, target);
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.vecna.ability", ability.id(), target.getDisplayName()), false);
		return 1;
	}

	/**
	 * {@code /garipseyler mindflayer status} – fight state and the boss's health/regeneration, {@code respawn} – the
	 * vanilla crystal ritual (four End crystals on the exit portal) done for you, and {@code attack <name> [player]} –
	 * make the nearest Mind Flayer use an attack now.
	 */
	private static LiteralArgumentBuilder<CommandSourceStack> mindFlayerCommand() {
		RequiredArgumentBuilder<CommandSourceStack, String> attack = Commands.argument("attack", StringArgumentType.word())
				.suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(MindFlayerAttack.values()).map(MindFlayerAttack::id), builder))
				.executes(context -> mindFlayerAttack(context, context.getSource().getPlayerOrException()))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(context -> mindFlayerAttack(context, EntityArgument.getPlayer(context, "player"))));
		return Commands.literal("mindflayer")
				.then(Commands.literal("status").executes(ModCommands::mindFlayerStatus))
				.then(Commands.literal("respawn").executes(ModCommands::mindFlayerRespawn))
				.then(Commands.literal("attack").then(attack));
	}

	private static int mindFlayerStatus(CommandContext<CommandSourceStack> context) {
		ServerLevel end = context.getSource().getServer().getLevel(Level.END);
		EnderDragonFight fight = end == null ? null : end.getDragonFight();
		if (!(fight instanceof MindFlayerFight flayerFight)) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.mind_flayer.no_fight"));
			return 0;
		}
		MindFlayer flayer = end.getEntities(ModEntities.MIND_FLAYER, LivingEntity::isAlive).stream().findFirst().orElse(null);
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.mind_flayer.fight",
				flayerFight.garipseylermc$isBossKilled(), fight.hasPreviouslyKilledDragon()), false);
		if (flayer != null) {
			context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.mind_flayer.status",
					String.format("%.1f", flayer.getHealth()), String.format("%.0f", flayer.getMaxHealth()),
					flayer.blockPosition().getX(), flayer.blockPosition().getY(), flayer.blockPosition().getZ(),
					flayer.ticksSinceDamage() / 20, flayer.isRegenerating()), false);
		}
		return flayer == null ? 0 : 1;
	}

	private static int mindFlayerRespawn(CommandContext<CommandSourceStack> context) {
		ServerLevel end = context.getSource().getServer().getLevel(Level.END);
		EnderDragonFight fight = end == null ? null : end.getDragonFight();
		BlockPos portal = fight instanceof MindFlayerFight flayerFight ? flayerFight.garipseylermc$exitPortal() : null;
		if (portal == null || !((MindFlayerFight) fight).garipseylermc$isBossKilled()) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.mind_flayer.cannot_respawn"));
			return 0;
		}
		BlockPos center = portal.above();
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos at = center.relative(direction, 3);
			if (end.getEntitiesOfClass(EndCrystal.class, new AABB(at)).isEmpty()) {
				EndCrystal crystal = new EndCrystal(end, at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
				crystal.setShowBottom(false);
				end.addFreshEntity(crystal);
			}
		}
		fight.tryRespawn();
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.mind_flayer.respawning"), true);
		return 1;
	}

	private static int mindFlayerAttack(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		String name = StringArgumentType.getString(context, "attack");
		MindFlayerAttack attack = Arrays.stream(MindFlayerAttack.values()).filter(a -> a.id().equals(name)).findFirst().orElse(null);
		if (attack == null) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.mind_flayer.unknown_attack", name));
			return 0;
		}
		List<MindFlayer> found = target.level().getEntitiesOfClass(MindFlayer.class, target.getBoundingBox().inflate(64.0), MindFlayer::isAlive);
		MindFlayer flayer = found.stream().min(Comparator.comparingDouble(f -> f.distanceToSqr(target))).orElse(null);
		if (flayer == null) {
			context.getSource().sendFailure(Component.translatable("commands.garipseylermc.mind_flayer.none_nearby"));
			return 0;
		}
		flayer.forceAttack(attack, target);
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.mind_flayer.attack", attack.id(), target.getDisplayName()), false);
		return 1;
	}

	/** Shows a player's vine drain timer and the distance to the nearest vined portal in the Nether. */
	private static int vineStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		int ticks = player.getAttachedOrCreate(ModAttachments.VINE_DRAIN_TICKS);
		int intervalTicks = ConfigManager.get().vine.drainIntervalMinutes * 1200;
		ServerLevel nether = context.getSource().getServer().getLevel(Level.NETHER);
		int sites = nether == null ? 0 : VineSites.get(nether).sites().size();
		double nearest = nether == null || player.level() != nether ? -1
				: VineSites.get(nether).sites().stream().mapToDouble(site -> Math.sqrt(site.center().distanceToSqr(player.position()))).min().orElse(-1);
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.vine.status",
				player.getDisplayName(), ticks / 20, intervalTicks / 20, sites, nearest < 0 ? "-" : String.format("%.1f", nearest)), false);
		return ticks;
	}
}
