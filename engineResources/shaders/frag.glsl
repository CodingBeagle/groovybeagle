#version 330 core
in vec2 TextureCoordinateOut;

out vec4 FragmentColor;

uniform sampler2D theTexture;

void main()
{
	FragmentColor = texture(theTexture, TextureCoordinateOut);
}