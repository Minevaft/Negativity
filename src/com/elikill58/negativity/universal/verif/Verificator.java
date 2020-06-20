package com.elikill58.negativity.universal.verif;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;

import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.Version;
import com.elikill58.negativity.universal.verif.storage.VerificationStorage;

public class Verificator {
	
	public static final int VERIFICATION_VERSION = 0;
	private static final Collector<Cheat, ?, Map<Cheat, VerifData>> COLLECTOR = Collectors.toMap(new Function<Cheat, Cheat>() {
					@Override
					public Cheat apply(Cheat t) {
						return t;
					}
				}, new Function<Cheat, VerifData>() {
					@Override
					public VerifData apply(Cheat t) {
						return new VerifData();
					}
				});
	
	private final Map<Cheat, VerifData> cheats;
	private final NegativityPlayer np;
	private final String asker;
	private final List<String> messages;
	private final int version;
	private final Version playerVersion;
	
	public Verificator(NegativityPlayer np, String asker) {
		this(np, asker, Cheat.CHEATS);
	}
	
	public Verificator(NegativityPlayer np, String asker, List<Cheat> list) {
		this(np, asker, list.stream().collect(COLLECTOR), new ArrayList<>(), VERIFICATION_VERSION, np.getPlayerVersion());
	}
	
	public Verificator(NegativityPlayer np, String asker, Map<Cheat, VerifData> cheats, List<String> messages, int version, Version playerVersion) {
		this.np = np;
		this.asker = asker;
		this.cheats = cheats;
		this.messages = messages;
		this.version = version;
		this.playerVersion = playerVersion;
	}

	public NegativityPlayer getNegativityPlayer() {
		return np;
	}
	
	public UUID getPlayerId() {
		return np.getUUID();
	}
	
	public String getAsker() {
		return asker;
	}

	public Map<Cheat, VerifData> getCheats() {
		return cheats;
	}
	
	public Optional<VerifData> getVerifData(Cheat c) {
		VerifData data = cheats.get(c);
		if(data != null)
			return Optional.of(data);
		return Optional.empty();
	}
	
	public List<String> getMessages(){
		return messages;
	}

	public int getVersion() {
		return version;
	}

	public Version getPlayerVersion() {
		return playerVersion;
	}

	public void generateMessage() {
		StringJoiner messageCheatNothing = new StringJoiner(", ");
		for(Entry<Cheat, VerifData> currentCheat : cheats.entrySet()) {
			Cheat c = currentCheat.getKey();
			VerifData data = currentCheat.getValue();
			if(data.hasSomething()) {
				String name = c.compile(data, np);
				if(name != null) {
					messages.add(ChatColor.GOLD + c.getName() + ChatColor.DARK_GRAY + ": " + ChatColor.GRAY + name);
					continue;
				}
			}
			messageCheatNothing.add(c.getName());
		}
		if(messageCheatNothing.length() > 0)
			messages.add("Nothing detected: " + messageCheatNothing.toString());
	}
	
	public void save() {
		if(messages.isEmpty())
			generateMessage();
		VerificationStorage.getStorage().saveVerification(this);
		/*File folder = new File(Adapter.getAdapter().getDataFolder().getAbsolutePath() + File.separator + "verif" + File.separator + np.getUUID());
		folder.mkdirs();
		JSONObject json = new JSONObject();
		json.put("startedBy", asker);
		json.put("result", messages);
		List<String> cheatNothing = new ArrayList<>();
		cheats.forEach((cheat, verif) -> {
			if(verif.hasSomething()) {
				json.put(cheat.getKey(), verif.getAllData().values().stream().filter(DataCounter::has).map(DataCounter::print).collect(Collectors.toList()));
			} else
				cheatNothing.add(cheat.getName());
		});
		json.put("cheat-nothing", cheatNothing);
		
		File resultFile = new File(folder, new Timestamp(System.currentTimeMillis()).toString().split("\\.")[0].replaceAll(" ", "_").replaceAll(":", "_") + ".json");
		try {
			if(!resultFile.exists())
				resultFile.createNewFile();
			Files.write(resultFile.toPath(), json.toJSONString().getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}