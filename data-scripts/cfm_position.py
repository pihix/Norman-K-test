import pandas as pd
import numpy as np
from functions import bank_asset_class_ref
import re



cfm_columns_mapping = {
    0: "bank_account_number",
    1: "code_bank_1",
    4: "code_bank_2",
    6: "isin",
    7: "bank_valor",
    9: "position_name",
    10: "position_ccy",
    12: "stock_exchange",
    14: "last_price",
    15: "last_price_date",
    16: "quantity",
    19: "minimum_quantity_trade",
    20: "quotation_type",
    21: "quotation_unit",
    22: "accrued_basis",
    23: "previous_coupon_date",
    24: "next_coupon_date",
    25: "maturity",
    27: "coupon_fqcy",
    28: "coupon_rate",
    33: "purchase_price",
    40: "strike",
    41: "call_or_put",
    42: "adjust_quotity",
    44: "rating_moody",
    45: "rating_s&p",
    50: "accrued_interest",
    51: "amount",
    53: "system_date"
}


words_to_remove = [
    "notes", "bonds", "bond", "note", "obligation", "debenture", "tranche",
    "euro", "medium", "term", "programme", "global", "senior", "secured",
    "unsecured", "min", "max", "step", "cap", "floater", "corp", "sa", "plc",
    "coupon", "issue", "rate", "emtn", "usd", "eur"
]


def standardize_bond_name(row):
    if row["sub_asset_class"] != "Bonds":
        return row["position_name"]

    # Coupon
    try:
        coupon = f"{float(row['coupon_rate']):.0f}%"
    except:
        coupon = ""

    # Nettoyage du nom
    name = str(row["position_name"]).lower()

    # Supprimer tous les mots-clés inutiles
    for word in words_to_remove:
        name = re.sub(rf"\b{word}\b", "", name, flags=re.IGNORECASE)

    # Nettoyer les doubles espaces
    name = re.sub(r"\s{2,}", " ", name).strip().title()

    # Date de maturité
    try:
        maturity_date = pd.to_datetime(row["maturity"], errors='coerce')
        maturity_str = maturity_date.strftime("%d-%m-%Y") if pd.notna(maturity_date) else ""
    except:
        maturity_str = ""

    return f"{coupon} {name} {maturity_str}".strip()



def cfm_position(date_close, dfs):

    try:
        df_source = dfs['conso_position_security_17']
    except KeyError:
        raise ValueError("Le DataFrame 'conso_position_security_17' est manquant dans dfs")

    # Sélection et renommage
    df = df_source.iloc[:, list(cfm_columns_mapping.keys())].copy()
    df.columns = list(cfm_columns_mapping.values())



    # Nettoyage des colonnes spécifiques :

    # 1. Garder la partie après le "/" pour stock_exchange
    df["stock_exchange"] = df["stock_exchange"].astype(str).str.split("/").str[-1].str.strip()

    # 2. Standardiser quotation_type
    df["quotation_type"] = df["quotation_type"].replace({
        "1 /COURS EN POURCENT": "Pourcentage",
        "0 /COURS PAR PIECE": "Unit"
    }).fillna(df["quotation_type"])

    # 3. Standardiser quotation_unit
    df["quotation_unit"] = df["quotation_unit"].replace({
        "001/NOMINAL": "Nominal",
        "002/1 PIECE": "Piece"
    }).fillna(df["quotation_unit"])
    df["quotation_type"] = df["quotation_type"].fillna("Unit")
    df["quotation_type"] = df["quotation_type"].apply(lambda x: x if x in ["Pourcentage", "Unit"] else "Unit") #si jamais un nouvelle entrée par défaut l'unit est 1


    # Création d'une colonne facteur en fonction de quotation_type
    df["quotation_factor"] = np.where(
        df["quotation_type"] == "Pourcentage",
        0.01,
        1
    )

    # 2. recalculation_total_amount = (last_price * quantity * facteur) + accrued_interest
    df["recalculation_total_amount"] = (
        df["last_price"].fillna(0) *
        df["quantity"].fillna(0) *
        df["quotation_factor"]
    ) + df["accrued_interest"].fillna(0)


    # 1. Total_amount = accrued_interest + amount_ccy_position
    df["total_amount"] = df["accrued_interest"].fillna(0) + df["amount"].fillna(0)


    # 4. Garder uniquement le chiffre avant le "/" pour coupon_fqcy
    df["coupon_fqcy"] = df["coupon_fqcy"].astype(str).str.split("/").str[0].str.strip()

    # 5. Garder uniquement le texte avant le "/" pour les notations
    df["rating_moody"] = df["rating_moody"].astype(str).str.split("/").str[0].str.strip()
    df["rating_s&p"] = df["rating_s&p"].astype(str).str.split("/").str[0].str.strip()

    # Conversion et formatage ISO des dates dès l'import
    date_cols = ["last_price_date", "previous_coupon_date", "next_coupon_date", "maturity", "system_date"]
    for col in date_cols:
        df[col] = pd.to_datetime(df[col], errors='coerce', dayfirst=True).dt.strftime("%Y-%m-%d")
    df["date_close"] = pd.to_datetime(date_close).strftime("%Y-%m-%d")

    # Conversion des numériques
    numeric_cols = [
        "last_price", "quantity", "minimum_quantity_trade", "coupon_rate",
        "purchase_price", "accrued_interest", "amount", "strike"
    ]
    for col in numeric_cols:
        df[col] = pd.to_numeric(df[col], errors='coerce')


    # Ajustements spécifiques
    df["position_ccy"] = df["position_ccy"].astype(str).str[:3]
    

    # Champs calculés
    df["code_mapping_position"] = df["code_bank_2"].astype(str) + df["code_bank_1"].astype(str)
    df["perf"] = (df["last_price"] / df["purchase_price"] - 1) * 100
    df["isin_or_bank_valor"] = np.where(df["isin"].isna(), df["bank_valor"], df["isin"])


    ### -------------   Mapping Asset Class avec référentiel banque  -------------------- ###
    df_ref_securities_bank = bank_asset_class_ref("cfm")
    df = df.merge(
        df_ref_securities_bank,
        on="code_mapping_position",
        how="left"
    )

    # Ajout d’un statut de classification
    df["classification_status"] = np.where(
        df["asset_class"].notna(), "bank_mapping", "No_mapping"
    )

    ## On standarise les noms des obligations pour que ca soit plus propre
    df["position_name"] = df.apply(standardize_bond_name, axis=1)
    df["position_name"] = df["position_name"].astype(str).str.title()


    # Export
    output_path = r"C:\Users\YoannAmblard\OneDrive - Norman-K\DayToDay\cfm_positions.xlsx"
    df.to_excel(output_path, index=False)
    print(f"Export Excel terminé : {output_path}")

    return df
