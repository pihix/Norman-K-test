package com.nimbleways.springboilerplate.testhelpers.annotations;

import org.jilt.Builder;
import org.jilt.BuilderStyle;

@Builder(factoryMethod = "start", className = "*BuildExecutor", style = BuilderStyle.STAGED, buildMethod = "execute")
public @interface ExecutableBuilder {
}
