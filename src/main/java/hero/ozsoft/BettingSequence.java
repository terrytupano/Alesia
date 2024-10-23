package hero.ozsoft;

import java.util.*;

import org.apache.commons.lang3.*;

import hero.ozsoft.actions.*;

public class BettingSequence {

    private List<String> sequence;
    public List<String> potShare;

    public BettingSequence() {
        this.sequence = new ArrayList<>();
        this.potShare = new ArrayList<>();
    }

    public void addAction(Player player, PlayerAction action) {
        String name = "";
        String action2 = action.toString();
        if (player != null)
            name = player.getName();

        if (action instanceof FoldAction || action instanceof CheckAction) {
            action2 = action.getName();
        }

        addMessage(name + " - " + action2);
    }

    public void addShare(String name, Integer ammount) {
        addShare(name, Double.valueOf(ammount));
    }

    public void addShare(String name, Double ammount) {
        this.sequence.add(name + ":" + ammount);
    }

    public void addMessage(String message, Object... args) {
        String message2 = String.format(message, args);
        sequence.add(message2);
    }

    public String getSequence() {
        String result = sequence.toString();
        result = result.replace(",", "\n");
        result = StringUtils.substringBetween(result, "[", "]");
        return result;
    }
}