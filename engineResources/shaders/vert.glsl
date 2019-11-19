#version 330 core

#extension GL_ARB_separate_shader_objects : enable

layout (location = 0) in vec4 vertex;

uniform mat4 model;
uniform mat4 projection;

out vec2 TextureCoordinateOut;

void main()
{
	TextureCoordinateOut = vertex.zw;
	gl_Position = projection * model * vec4(vertex.xy, 0.0, 1.0);
}