package org.frags.complexInteractions.objects.conversation.factories;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.Requirement;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemProvider;
import org.frags.complexInteractions.objects.conversation.matchers.MaterialMatcher;
import org.frags.complexInteractions.objects.conversation.matchers.ModelDataMatcher;
import org.frags.complexInteractions.objects.conversation.matchers.NormalMatcher;
import org.frags.complexInteractions.objects.conversation.matchers.PDCMatcher;
import org.frags.complexInteractions.objects.conversation.requirements.ConditionRequirement;
import org.frags.complexInteractions.objects.conversation.requirements.ItemRequirement;
import org.frags.complexInteractions.objects.conversation.requirements.PermissionRequirement;

public class RequirementFactory {

    public static Requirement parse(String line, ItemProvider provider, String failMessage) {

        if (line.startsWith("has_item:")) {
            return parseItem(line.replace("has_item:", ""), provider, failMessage);
        }

        if (line.startsWith("has_permission:")) {
            return new PermissionRequirement(failMessage, line.replace("has_permission:", ""));
        }

        if (line.startsWith("condition:")) {
            return new ConditionRequirement(failMessage, line.replace("condition:", ""));
        }

        return null;
    }

    private static ItemRequirement parseItem(String params, ItemProvider provider, String failMessage) {

        String[] parts = params.split(" ");
        if (parts.length < 2) return null;

        String definition = parts[0];
        int amount;

        try {
            amount = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }

        ItemMatcher matcher = null;

        if (definition.startsWith("preset")) {
            //preset:(name) 1
            String itemId = definition.split(":", 2)[1];
            ItemStack loadedItem =  provider.getItem(itemId);

            if (loadedItem != null) {
                matcher = new NormalMatcher(loadedItem);
            }
        } else if (definition.startsWith("material")) {
            //material:(material) 1
            String matName = definition.split(":", 2)[1];
            Material mat = Material.valueOf(matName);
            matcher = new MaterialMatcher(mat);
        } else if (definition.startsWith("pdc")) {
            //pdc:namespace:key=value
            try {
                String rawPdc = definition.split(":", 2)[1];
                String[] pdcParts = rawPdc.split(":");
                String namespace =  pdcParts[0];
                String key =  pdcParts[1];

                if (key.contains("=")) {
                    String[] keyValue = key.split("=");
                    matcher = new PDCMatcher(namespace, keyValue[0], keyValue[1]);
                } else {
                    matcher = new PDCMatcher(namespace, key, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else if (definition.startsWith("model")) {
            //model:(model) 10
            int model =  Integer.parseInt(definition.split(":", 2)[1]);
            matcher = new ModelDataMatcher(model);
        }

        if (matcher != null) {
            return new ItemRequirement(failMessage, matcher, amount);
        }

        return null;
    }
}
