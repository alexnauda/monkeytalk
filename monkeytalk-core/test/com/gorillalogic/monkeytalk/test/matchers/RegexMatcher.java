package com.gorillalogic.monkeytalk.test.matchers;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class RegexMatcher extends TypeSafeMatcher<String> {
        private final Pattern pattern;

        public RegexMatcher(String regex) {
                this.pattern = Pattern.compile(regex);
        }

        @Override
        public void describeTo(Description description) {
                description.appendText("regex pattern ").appendValue(pattern);
        }

        @Override
        public boolean matchesSafely(String item) {
                return pattern.matcher(item).matches();
        }

        @Factory
        public static Matcher<String> regex(String regex) {
                return new RegexMatcher(regex);
        }
}
