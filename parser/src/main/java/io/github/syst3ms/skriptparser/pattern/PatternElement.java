package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The superclass of all elements of a pattern.
 */
public interface PatternElement {

    /**
     * Attempts to match the {@link PatternElement} to a string at a specified index.
     * About the index, make sure to never increment the index by some arbitrary value when returning
     *
     * @param s the string to match this PatternElement against
     * @param index the index of the string at which this PatternElement should be matched
     * @return the index at which the matching should continue afterwards if successful. Otherwise, {@literal -1}
     */
    int match(String s, int index, MatchContext context);

    static List<PatternElement> flatten(PatternElement element) {
        if (element instanceof CompoundElement) {
            return ((CompoundElement) element).getElements();
        } else {
            return Collections.singletonList(element);
        }
    }

    /**
     * This method should return all text components that will always be present,
     * no matter how the pattern is used.
     * @param element the element
     * @return the always-present text elements of this pattern
     */
    static List<String> getKeywords(PatternElement element) {
        return flatten(element).stream()
                .filter(el -> el instanceof TextElement)
                .map(el -> ((TextElement) el).getText().strip().toLowerCase())
                .collect(Collectors.toList());
    }

    static List<PatternElement> getPossibleInputs(List<PatternElement> elements) {
        List<PatternElement> optionalPossibilities = new ArrayList<>(); // We generally want to get the non-optional ones out of the way first
        List<PatternElement> possibilities = new ArrayList<>();
        for (var element : elements) {
            if (element instanceof TextElement || element instanceof RegexGroup) {
                if (element instanceof TextElement) {
                    var text = ((TextElement) element).getText();
                    if (text.isEmpty() || text.isBlank() && elements.size() == 1) {
                        return possibilities;
                    } else if (text.isBlank()) {
                        continue;
                    }
                }
                possibilities.add(element);
                possibilities.addAll(optionalPossibilities);
                return possibilities;
            } else if (element instanceof ChoiceGroup) {
                for (var choice : ((ChoiceGroup) element).getChoices()) {
                    var possibleInputs = getPossibleInputs(flatten(choice.getElement()));
                    possibilities.addAll(possibleInputs);
                }
                possibilities.addAll(optionalPossibilities);
                return possibilities;
            } else if (element instanceof ExpressionElement) {
                possibilities.add(element);
                possibilities.addAll(optionalPossibilities);
                return possibilities;
            } else if (element instanceof OptionalGroup) {
                optionalPossibilities.addAll(getPossibleInputs(flatten(((OptionalGroup) element).getElement())));
            }
        }
        possibilities.addAll(optionalPossibilities);
        possibilities.add(new TextElement("\0")); // EOL still goes at the very end
        return possibilities;
    }
}
