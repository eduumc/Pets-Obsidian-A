package us.edumc.obsidianstudios.pets.util;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import us.edumc.obsidianstudios.pets.PetsObsidian;

public class FormulaEvaluator {

    public static double evaluate(String formula, int level) {
        try {
            Expression expression = new ExpressionBuilder(formula)
                    .variable("level")
                    .build()
                    .setVariable("level", level);
            return expression.evaluate();
        } catch (Exception e) {
            PetsObsidian.getInstance().getLogger().warning("Fórmula de XP inválida: " + formula);
            return 100 * level;
        }
    }
}