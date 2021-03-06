package com.gadarts.necronemes.systems.render.shaders;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.gadarts.necronemes.components.ComponentsMapper;

import static com.badlogic.gdx.graphics.GL20.*;

public class DepthMapShader extends BaseShader {
	public Renderable renderable;

	public DepthMapShader(final Renderable renderable, final ShaderProgram shaderProgramModelBorder) {
		this.renderable = renderable;
		this.program = shaderProgramModelBorder;
		register(DefaultShader.Inputs.worldTrans, DefaultShader.Setters.worldTrans);
		register(DefaultShader.Inputs.projViewTrans, DefaultShader.Setters.projViewTrans);
		register(DefaultShader.Inputs.normalMatrix, DefaultShader.Setters.normalMatrix);

	}

	@Override
	public void end( ) {
		super.end();
	}

	@Override
	public void begin(final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		context.setDepthTest(GL_LEQUAL);
		context.setCullFace(GL_BACK);
	}

	@Override
	public void render(final Renderable renderable) {
		context.setBlending(renderable.material.has(BlendingAttribute.Type), GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		super.render(renderable);
	}

	@Override
	public void init( ) {
		final ShaderProgram program = this.program;
		this.program = null;
		init(program, renderable);
		renderable = null;
	}

	@Override
	public int compareTo(final Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(final Renderable instance) {
		return true;
	}

	@Override
	public void render(final Renderable renderable, final Attributes combinedAttributes) {
		super.render(renderable, combinedAttributes);
	}

}
