---
name: rork toolkit
description: An instruction for using AI features like AI chats, agents, text/object generation, image generation/editing, and speech-to-text. Alsways read it when working on features related to AI.
---

You can build app that use AI.

Never install @rork-ai/toolkit-sdk, it is always pre-installed.
We do it using typescript path mapping.
So never expect it to be in package.json, nor in bun.lock, nor install it.

<agent-llm>
Api route to make a request to AI: new URL("/agent/chat", process.env["EXPO_PUBLIC_TOOLKIT_URL"])
Messages are in the Vercel AI v5 SDK format, including images.

import { createRorkTool, useRorkAgent } from "@rork-ai/toolkit-sdk";
import { ToolUIPart } from "ai";

Here's an example of how to use the API:
<example>
const [input, setInput] = useState("");
const { messages, error, sendMessage, addToolResult, setMessages } = useRorkAgent({
tools: {
addTodo: createRorkTool({
description: "Add todo",
zodSchema: z.object({
title: z.string().describe("Short description of the todo item"),
description: z
.string()
.describe("Detailed description of the todo item")
.optional(),
dueDate: z
.string()
.describe("Optional due date in ISO 8601 format")
.optional(),
priority: z
enum(["low", "medium", "high"])
.describe("Priority level of the todo")
.optional(),
tags: z
.array(z.string())
.describe("Optional list of tags for categorization")
.optional(),
}),
// execute is optional, if you don't provide it, the tool will not be executed automatically
// and you will have to call it manually with addToolResult
execute(input) {
someState.addToDo({
title: input.title,
description: input.description,
dueDate: input.dueDate,
priority: input.priority,
tags: input.tags,
});
},
}),
},
});

    /*
    send message is a function that sends a message to the AI
    it accepts a string or a message object with the following properties:
    type MessageObject = {text: string, files?: File[]}

    type File = {
        type: "file";
        mimeType: string;
        uri: string; // base64 or file://
    }
    */



    // this is how you render the messages in the UI
    messages.map((m) => (
        <View key={m.id} style={{ marginVertical: 8 }}>
        <View style={{ flexDirection: "column", gap: 4 }}>
            <Text style={{ fontWeight: 700 }}>{m.role}</Text>
            {m.parts.map((part, i) => {
            switch (part.type) {
                case "text":
                return (
                    <View key={`${m.id}-${i}`}>
                    <Text>{part.text}</Text>
                    </View>
                );
                case "tool":
                const toolName = part.toolName;

                switch (part.state) {
                    case "input-streaming":
                    case "input-available":
                    // Automatically streamed partial inputs
                    // access part.input to see input values

                    return (
                        <View key={`${m.id}-${i}`}>
                        <Text>Calling {toolName}...</Text>
                        </View>
                    );

                    case "output-available":
                    return (
                        <View key={`${m.id}-${i}`}>
                        <Text>
                            Called {JSON.stringify(part.output, null, 2)}
                        </Text>
                        </View>
                    );

                    case "output-error":
                    // Explicit error state with information
                    return (
                        <View key={`${m.id}-${i}`}>
                        <Text>Error: {part.errorText}</Text>
                        </View>
                    );
                }
            }
            })}
        </View>
        </View>
    ))

</example>
</agent-llm>

<llm>
import { generateObject, generateText } from "@rork-ai/toolkit-sdk";

type TextPart = { type: "text"; text: string };
type ImagePart = { type: "image"; image: string };
type UserMessage = { role: "user"; content: string | (TextPart | ImagePart)[] };
type AssistantMessage = { role: "assistant"; content: string | TextPart[] };

// use generateObject and generate text only if you need a single generation.
// When the chat history and agentic flows are not needed
// For example, parsing image to text in mutation.
// or generating a caption for image, or a summary

// below are the functions you can use to generate text or objects
export async function generateObject<T extends z.ZodType>(params: {
messages: (UserMessage | AssistantMessage)[];
schema: T;
}): Promise<z.infer<T>>;

export async function generateText(
params: string | { messages: (UserMessage | AssistantMessage)[] },
): Promise<string>;
</llm>

<generate-images>
Api route to generate images: https://toolkit.rork.com/images/generate/
It is a POST route that accepts a JSON body with { prompt: string, size?: string }.
size is optional, for example "1024x1024" or "1024x1792" or "1792x1024".
It returns a JSON object: { image: { base64Data: string; mimeType: string; }, size: string }
Uses DALL-E 3.

Use these TypeScript types for references:
type ImageGenerateRequest = { prompt: string, size?: string }
type ImageGenerateResponse = { image: { base64Data: string; mimeType: string; }, size: string }
</generate-images>

<edit-images>
Api route to edit images: https://toolkit.rork.com/images/edit/
It is a POST route that accepts a JSON body with:
- prompt: string (required) - the text instruction for how to edit the image
- images: Array<{ type: 'image'; image: string }> (required) - one or more base64 encoded images
- aspectRatio: string (optional) - the aspect ratio for the edited image

Allowed aspect ratios: "1:1", "2:3", "3:2", "3:4", "4:3", "4:5", "5:4", "9:16", "16:9", "21:9"
Default aspect ratio: "16:9"

It returns a JSON object: { image: { base64Data: string; mimeType: string; aspectRatio: string; } }
Uses Google Gemini 2.5 Flash Image (gemini-2.5-flash-image).

Use these TypeScript types for references:
type ImageEditRequest = {
prompt: string;
images: Array<{ type: 'image'; image: string }>; // base64 encoded
aspectRatio?: string; // optional, defaults to "16:9"
}
type ImageEditResponse = {
image: {
base64Data: string;
mimeType: string;
aspectRatio: string;
}
}
</edit-images>

<speech-to-text>
Api route for speech-to-text: https://toolkit.rork.com/stt/transcribe/
- It is a POST route that accepts FormData with audio file and optional language.
- It returns a JSON object: { text: string, language: string }
- Supports mp3, mp4, mpeg, mpga, m4a, wav, and webm audio formats and auto-language detection.
- When using FormData for file uploads, never manually set the Content-Type header - let the browser handle it automatically.
- After stopping recording: Mobile - disable recording mode with Audio.setAudioModeAsync({ allowsRecordingIOS: false }). Web - stop all stream tracks with stream.getTracks().forEach(track => track.stop())
- Note: For Platform.OS === 'web', use Web Audio API (MediaRecorder) for audio recording. For mobile, use expo-av.

When using expo-av for audio recording, always configure the recording format to output .wav for IOS and .m4a for Android by adding these options to prepareToRecordAsync().
Here's an example of how to configure the recording format:
<example>
await recording.prepareToRecordAsync({
android: {
extension: '.m4a',
outputFormat: Audio.RECORDING_OPTION_ANDROID_OUTPUT_FORMAT_MPEG_4,
audioEncoder: Audio.RECORDING_OPTION_ANDROID_AUDIO_ENCODER_AAC,
},
ios: {
extension: '.wav',
outputFormat: Audio.RECORDING_OPTION_IOS_OUTPUT_FORMAT_LINEARPCM,
audioQuality: Audio.RECORDING_OPTION_IOS_AUDIO_QUALITY_HIGH,
},
});
</example>

ALWAYS append audio to formData as { uri, name, type } for IOS/Android before sending it to the speech-to-text API.
Here's an example of how to append the audio to formData:
<example>
const uri = recording.getURI();
const uriParts = uri.split('.');
const fileType = uriParts[uriParts.length - 1];

    const audioFile = {
    uri,
    name: "recording." + fileType,
    type: "audio/" + fileType
    };

    formData.append('audio', audioFile);

</example>

Use these TypeScript types for references:
type STTRequest = { audio: File, language?: string }
type STTResponse = { text: string, language: string }

Handle errors and set proper state after the request is done.
</speech-to-text>
