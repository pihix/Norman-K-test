import pandas as pd
import posixpath

def SFTP_CFM(ssh, sftp, remote_dir):
    file_suffixes = {
        '000017': 'position_security_17',
        '000019': 'position_cash_19',
        '000069': 'mvt_cash_69',
        '000036': 'frd_deposit_36',
        '17.csv': 'AM_position_security_17',
        '19.csv': 'AM_position_cash_19',
        '69.csv': 'AM_mvt_cash_69',
        '36.csv': 'AM_frd_deposit_36',
        '18.csv': 'AM_mvt_security_18',
        '000018': 'mvt_security_18'
    }
    downloaded_files = {}
    consolidated_dataframes = {}

    stdin, stdout, stderr = ssh.exec_command(f'cd {remote_dir}; ls -1t | head -10')
    files = [line.strip() for line in stdout.readlines()]

    date_close = None
    for fname in files:
        if fname.endswith('000017') and date_close is None:
            try:
                remote_path = posixpath.join(remote_dir, fname.strip())
                with sftp.open(remote_path) as remote_file:
                    df = pd.read_csv(remote_file, delimiter=";", encoding='latin-1', usecols=[53], names=['date'], header=None)
                    date_close = pd.to_datetime(df['date'].iloc[0], format='%d/%m/%Y').strftime('%Y%m%d')
            except:
                continue
            break

    if not date_close:
        raise ValueError("Impossible de déterminer la date de référence depuis un fichier 000017")

    stdin, stdout, stderr = ssh.exec_command(f'cd {remote_dir}; ls -1t | head -12')
    files_full = [line.strip() for line in stdout.readlines()]
    has_16 = any(f.endswith('000016') or f.endswith('16.csv') for f in files_full)

    files = files_full if has_16 else [line.strip() for line in ssh.exec_command(f'cd {remote_dir}; ls -1t | head -10')[1].readlines()]

    for fname in files:
        for suffix, label in file_suffixes.items():
            if fname.endswith(suffix) and suffix not in downloaded_files:
                remote_path = posixpath.join(remote_dir, fname.strip())
                try:
                    with sftp.open(remote_path) as remote_file:
                        df = pd.read_csv(remote_file, header=None, delimiter=";", encoding='latin-1')
                        downloaded_files[suffix] = df
                except:
                    continue

    # Création de labels explicites pour les DataFrames consolidés
    suffix_to_label = {
        '17': 'conso_position_security_17',
        '18': 'conso_mvt_security_18',
        '19': 'conso_position_cash_19',
        '36': 'conso_frd_deposit_36',
        '69': 'conso_mvt_cash_69'
    }

    for code, label in suffix_to_label.items():
        base_key = f'0000{code}'
        am_key = f'{code}.csv'
        df1 = downloaded_files.get(base_key, pd.DataFrame())
        df2 = downloaded_files.get(am_key, pd.DataFrame())
        df_final = pd.concat([df1, df2], axis=0)
        if code == '36':
            df_final.insert(16, 16, date_close)
        if code == '19':
            df_final.insert(9, 9, date_close)
        consolidated_dataframes[label] = df_final

    return date_close, consolidated_dataframes
