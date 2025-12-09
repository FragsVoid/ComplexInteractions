package org.frags.complexInteractions.objects.conversation.requirements;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.Requirement;

public class ConditionRequirement extends Requirement {

    private final String condition;

    public ConditionRequirement(String failMessage, String condition) {
        super(failMessage);
        this.condition = condition;
    }

    @Override
    public boolean check(Player player) {
        String parsed = PlaceholderAPI.setPlaceholders(player, condition);
        return evaluateExpression(parsed);
    }

    private boolean evaluateExpression(String expression) {
        expression = expression.trim();

        String[] operators = {">=", "<=", "==", "!=", ">", "<"};

        String operatorUsed = null;

        for (String op : operators) {
            if (expression.contains(op)) {
                operatorUsed = op;
                break;
            }
        }

        if (operatorUsed == null) {
            return Boolean.parseBoolean(expression);
        }

        String[] parts = expression.split(java.util.regex.Pattern.quote(operatorUsed));

        if (parts.length < 2) return false;

        String leftStr = parts[0].trim();
        String rightStr = parts[1].trim();

        try {
            double leftNum = Double.parseDouble(leftStr);
            double rightNum = Double.parseDouble(rightStr);
            return compareNumbers(leftNum, rightNum, operatorUsed);
        } catch (NumberFormatException e) {
            return compareStrings(leftStr, rightStr, operatorUsed);
        }
    }

    private boolean compareNumbers(double a, double b, String operator) {
        switch (operator) {
            case ">": return a > b;
            case "<": return a < b;
            case ">=": return a >= b;
            case "<=": return a <= b;
            case "==": return a == b;
            case "!=": return a != b;
            default: return false;
        }
    }

    private boolean compareStrings(String a, String b, String operator) {
        switch (operator) {
            case "==": return a.equals(b);
            case "!=": return !a.equals(b);
            default: return false;
        }
    }
}
