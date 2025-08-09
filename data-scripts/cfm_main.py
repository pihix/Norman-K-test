from SFTP_Connexion import connect_sftp
from cfm_sftp import SFTP_CFM
from cfm_position import cfm_position
from datetime import datetime, timedelta


def get_expected_data_date():

    now = datetime.now()
    hour = now.hour
    today_str = now.strftime("%Y%m%d")
    yesterday_str = (now - timedelta(days=1)).strftime("%Y%m%d")

    if now.weekday() >= 5:
        return None

    return yesterday_str if hour < 20 else today_str


# Parameters for connexion to CFM (or general system)
hostname = "51.159.89.98"
port = 22
username = "m3dev"
private_key_path = r"C:\Users\YoannAmblard\.ssh\id_sftp_client"
remote_path = "/home/cfm/import"

# SFTP Connection
ssh, sftp = connect_sftp(hostname, port, username, private_key_path)

# Processing CFM SFTP files
date_close, dfs = SFTP_CFM(ssh, sftp, remote_path)


# Expected date based on current time and day
expected_date = get_expected_data_date()

# Display results
print("Received closing date:", date_close)
print("Expected closing date:", expected_date)


# --- Handle below in the frontend: status for missing data "Missing Data" ---

if expected_date is None:
    print("🟢 No file expected today (weekend).")
elif date_close == expected_date:
    print(f"✅ Data successfully received for {expected_date}")
    cfm_position(date_close, dfs)
else:
    print(f"⚠️ Missing data for {expected_date} (received for {date_close})")
    # You can send an email or log an alert here
    

# Closing connexion
sftp.close()
ssh.close()