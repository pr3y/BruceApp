import esptool
import sys
import io
from android.content import Context
from serial.android import set_android_context


def upload_firmware(context: Context, arguments: str):
    set_android_context(context)
    
    # Capture stdout and stderr to get detailed output
    old_stdout = sys.stdout
    old_stderr = sys.stderr
    captured_output = io.StringIO()
    captured_error = io.StringIO()
    
    try:
        sys.stdout = captured_output
        sys.stderr = captured_error
        
        # Run esptool
        esptool.main(arguments.split(" "))
        
        # Get the captured output
        output = captured_output.getvalue()
        error = captured_error.getvalue()
        
        # Combine output and error
        combined_output = ""
        if output:
            combined_output += output
        if error:
            combined_output += error
            
        # Return the combined output instead of printing
        if combined_output.strip():
            return combined_output.strip()
        else:
            return "ESPTool completed successfully"
            
    except Exception as e:
        return f"ESPTool Exception: {str(e)}"
    finally:
        # Restore stdout and stderr
        sys.stdout = old_stdout
        sys.stderr = old_stderr
