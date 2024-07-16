# kubectl port-forward service/ai-chat-server 5000:5000

import gradio as gr
import requests

# Function to query the Flask AI chat server
def query_flask_server(prompt, file_content):
    url = 'http://localhost:5000/request'  # Adjust URL based on your server setup
    payload = {
        'prompt': prompt,
        'content': file_content  # Ensure 'content' matches your server's expected key for file content
    }
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        return response.json().get('ai_answer', 'No answer received')  # Adjust based on server response format
    else:
        return f"Error: {response.status_code} - {response.json().get('error', 'Unknown error')}"

# Function to define the Gradio interface
def gradio_interface(prompt, file_content):
    return query_flask_server(prompt, file_content)

# Define Gradio interface with Textboxes for input
iface = gr.Interface(
    fn=gradio_interface,
    inputs=[
        gr.Textbox(lines=2, label="Enter your prompt here..."),
        gr.Textbox(lines=10, label="Paste your file content here...")
    ],
    outputs=gr.Textbox(label="AI Answer"),
    title="AI Chat Server",
    description="Interact with the AI Chat Server using Gradio"
)

# Launch the Gradio interface
if __name__ == "__main__":
    iface.launch()
