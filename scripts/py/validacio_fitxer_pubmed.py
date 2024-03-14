from lxml import etree

# Assuming you've downloaded the DTD and saved it as 'pubmed_240101.dtd' in the current directory
dtd_file = './input/pubmed_240101.dtd'

# Load the DTD from the local file
with open(dtd_file, 'r') as dtd_content:
    dtd = etree.DTD(dtd_content)

# Step 2: Load your XML file
# Replace 'your_xml_file.xml' with the path to your actual XML file
xml_document = etree.parse('./input/pubmed24n0001.xml')

# Step 3: Validate the XML document against the DTD
is_valid = dtd.validate(xml_document)

print(f"Document is {'valid' if is_valid else 'invalid'} according to the DTD.")

# If the document is invalid, print the reason
if not is_valid:
    print(dtd.error_log.filter_from_errors()[0])
