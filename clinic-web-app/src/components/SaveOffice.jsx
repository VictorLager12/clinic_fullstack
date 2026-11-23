import { useCallback, useEffect, useState } from "react";
import axios from "axios";

const API_URL = "http://localhost:8082/api/offices";

const getErrorMessage = (error) => {
  if (!error.response) {
    return "Cannot reach the backend. Check that Spring Boot is running on port 8082.";
  }
  if (Array.isArray(error.response.data)) {
    return error.response.data.join("\n");
  }
  if (typeof error.response.data === "string") {
    return error.response.data;
  }
  return "The backend rejected the request with status " + error.response.status + " but sent no message.";
};

function SaveOffice() {
  const [officeName, setOfficeName] = useState("");
  const [officeNumber, setOfficeNumber] = useState("");
  const [offices, setOffices] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [editingId, setEditingId] = useState(null);

  const loadOffices = useCallback(() => {
    axios
      .get(API_URL)
      .then((response) => {
        setOffices(response.data);
      })
      .catch((error) => {
        alert(getErrorMessage(error));
      });
  }, []);

  useEffect(() => {
    loadOffices();
  }, [loadOffices]);

  const validateOffice = () => {
    const validationErrors = [];
    if (!officeName.trim()) {
      validationErrors.push("Office name is required");
    }
    if (!officeNumber || Number(officeNumber) < 1 || !Number.isInteger(Number(officeNumber))) {
      validationErrors.push("Office number must be a positive whole number");
    }
    return validationErrors;
  };

  const saveOffice = () => {
    const validationErrors = validateOffice();
    if (validationErrors.length > 0) {
      alert(validationErrors.join("\n"));
      return;
    }

    const office = {
      name: officeName.trim(),
      officeNumber: Number(officeNumber),
    };

    if (editingId) {
      axios
        .put(API_URL + "/" + editingId, office)
        .then((response) => {
          alert(response.data);
          resetForm();
          loadOffices();
        })
        .catch((error) => {
          alert(getErrorMessage(error));
        });
    } else {
      axios
        .post(API_URL + "/save", office)
        .then((response) => {
          alert(response.data);
          resetForm();
          loadOffices();
        })
        .catch((error) => {
          alert(getErrorMessage(error));
        });
    }
  };

  const resetForm = () => {
    setOfficeName("");
    setOfficeNumber("");
    setEditingId(null);
  };

  const startUpdate = (office) => {
    setEditingId(office.id);
    setOfficeName(office.name);
    setOfficeNumber(String(office.officeNumber));
  };

  const deleteOffice = (office) => {
    if (!window.confirm("Delete office " + office.name + "?")) {
      return;
    }

    axios
      .delete(API_URL + "/" + office.id)
      .then(() => {
        alert("Office deleted successfully");
        if (editingId === office.id) {
          resetForm();
        }
        loadOffices();
      })
      .catch((error) => {
        alert(getErrorMessage(error));
      });
  };

  const filteredOffices = offices.filter((office) => {
    const search = searchText.trim().toLowerCase();
    if (!search) {
      return true;
    }
    return (
      office.name.toLowerCase().includes(search) ||
      String(office.officeNumber).includes(search)
    );
  });

  return (
    <center>
      <h1>Save Office</h1>
      <p>
        <label htmlFor="officeName">Office Name: </label>
        <input
          id="officeName"
          name="officeName"
          type="text"
          value={officeName}
          onChange={(event) => setOfficeName(event.target.value)}
        />
      </p>
      <p>
        <label htmlFor="officeNumber">Office Number: </label>
        <input
          id="officeNumber"
          name="officeNumber"
          type="number"
          min="1"
          step="1"
          value={officeNumber}
          onChange={(event) => setOfficeNumber(event.target.value)}
        />
      </p>
      <button onClick={saveOffice}>{editingId ? "Update Office" : "Save Office"}</button>
      {editingId && <button onClick={resetForm}>Cancel Update</button>}

      <h2>Available Offices</h2>
      <p>
        <label htmlFor="officeSearch">Search: </label>
        <input
          id="officeSearch"
          name="officeSearch"
          type="text"
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
        />
      </p>
      <table border="1">
        <thead>
          <tr>
            <th>Office Name</th>
            <th>Office Number</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredOffices.map((office) => (
            <tr key={office.id}>
              <td>{office.name}</td>
              <td>{office.officeNumber}</td>
              <td>
                <button onClick={() => startUpdate(office)}>Update</button>
                <button onClick={() => deleteOffice(office)}>Delete</button>
              </td>
            </tr>
          ))}
          {filteredOffices.length === 0 && (
            <tr>
              <td colSpan="3">No offices found</td>
            </tr>
          )}
        </tbody>
      </table>
    </center>
  );
}

export default SaveOffice;